package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.{NewExpenseDto, UserNameDto}
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.domain.usecases.{AddExpenseUseCase, AddGroupUseCase, AddMemberUseCase, AddUserUseCase, AssembleGroupResponseUseCase, AssembleGroupsResponseUseCase, GetAllUsersUseCase, UpdateGroupUseCase}
import com.github.ai.split.entity.{NewExpense, NewGroup, NewUser, Split, SplitBetweenAll, SplitBetweenMembers}
import com.github.ai.split.api.request.{PostGroupRequest, PutGroupRequest}
import com.github.ai.split.api.response.{GetGroupsResponse, PostGroupResponse, PutGroupResponse}
import com.github.ai.split.entity.db.{ExpenseEntity, UserEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{asUid, parse, parsePasswordParam, parseUidFromUrl, some}
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

import java.util.UUID

class GroupController(
  private val accessResolver: AccessResolverService,
  private val addUserUseCase: AddUserUseCase,
  private val addMemberUseCase: AddMemberUseCase,
  private val addGroupUseCase: AddGroupUseCase,
  private val addExpenseUseCase: AddExpenseUseCase,
  private val getAllUsersUseCase: GetAllUsersUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase,
  private val assembleGroupsUseCase: AssembleGroupsResponseUseCase,
  private val updateGroupUseCase: UpdateGroupUseCase
) {

  def getGroups(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      groupUids <- parseUids(request)
      passwords <- parsePasswords(request)
      _ <- accessResolver.canAccessToGroups(groupUids = groupUids, passwords = passwords)
      groups <- assembleGroupsUseCase.assembleGroupDtos(uids = groupUids)
    } yield Response.text(GetGroupsResponse(groups).toJsonPretty + "\n")
  }

  def updateGroup(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      groupUid <- parseUidFromUrl(request)
      password <- parsePasswordParam(request)
      _ <- accessResolver.canAccessToGroup(groupUid = groupUid, password = password)

      data <- request.body.parse[PutGroupRequest]
      _ <- validateRequestData(data)

      newMembers <- {
        val newMembers = data.members.getOrElse(List.empty)
        if (newMembers.nonEmpty) {
          ZIO.collectAll(
              newMembers.map(member => member.uid.asUid())
            )
            .map(uids => Some(uids))
        } else {
          ZIO.succeed(None)
        }
      }

      _ <- updateGroupUseCase.updateGroup(
        groupUid = groupUid,
        newPassword = data.password.map(_.trim).filter(_.nonEmpty),
        newTitle = data.title.map(_.trim).filter(_.nonEmpty),
        newDescription = data.description.map(_.trim).filter(_.nonEmpty),
        newMemberUids = newMembers
      )

      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid = groupUid)
    } yield Response.text(PutGroupResponse(groupDto).toJsonPretty)
  }

  def createGroup(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      data <- request.body.parse[PostGroupRequest]

      // TODO: check users are distinct
      _ <- validateRequestData(data)

      newUsers <- if (data.members.nonEmpty) {
        ZIO.collectAll(
          data.members.getOrElse(List.empty)
            .map { member =>
              addUserUseCase.addUser(
                NewUser(
                  name = member.name
                )
              )
            }
        )
      } else {
        ZIO.succeed(List.empty)
      }

      newGroup <- addGroupUseCase.addGroup(
        NewGroup(
          password = data.password,
          title = data.title,
          description = data.description.getOrElse("")
        )
      )

      newMembers <- if (newUsers.nonEmpty) {
        ZIO.collectAll(
          newUsers.map { user =>
            addMemberUseCase.addMember(
              groupUid = newGroup.uid,
              userUid = user.uid
            )
          }
        )
      } else {
        ZIO.succeed(List.empty)
      }

      newExpenses <- if (data.expenses.nonEmpty) {
        addExpenses(
          groupUid = newGroup.uid,
          expenses = data.expenses.getOrElse(List.empty),
          members = newUsers
        )
      } else {
        ZIO.succeed(List.empty)
      }

      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid = newGroup.uid)
    } yield Response.text(
      text = PostGroupResponse(groupDto).toJsonPretty
    )
  }

  private def addExpenses(
    groupUid: UUID,
    expenses: List[NewExpenseDto],
    members: List[UserEntity]
  ): IO[DomainError, List[ExpenseEntity]] = {
    val userNameToUserMap = members.map(member => (member.name, member)).toMap

    for {
      newExpenses <- {
        ZIO.collectAll(
          expenses.map { expense =>
            val paidBy = ZIO.collectAll(
              expense.paidBy.map { payer =>
                ZIO.fromOption(userNameToUserMap.get(payer.name))
                  .map(user => user.uid)
                  .mapError(_ =>
                    DomainError(message = s"Unable to find user by name: ${payer.name}".some)
                  )
              }
            )

            val split: IO[DomainError, Split] = if (expense.isSplitBetweenAll.getOrElse(false)) {
              ZIO.succeed(SplitBetweenAll)
            } else {
              ZIO.collectAll(
                  expense.splitBetween.getOrElse(List.empty)
                    .map(splitUser =>
                      ZIO.fromOption(userNameToUserMap.get(splitUser.name))
                        .map(user => user.uid)
                        .mapError(_ =>
                          DomainError(message = s"Unable to find user by name: ${splitUser.name}".some)
                        )
                    )
                )
                .map(uids => SplitBetweenMembers(userUids = uids))
            }

            for {
              p <- paidBy
              s <- split
            } yield NewExpense(
              title = expense.title,
              description = expense.description.getOrElse(""),
              amount = expense.amount,
              paidBy = p,
              split = s
            )
          }
        )
      }

      e <- ZIO.collectAll(
        newExpenses.map { newExpense =>
          addExpenseUseCase.addExpenseToGroup(
            groupUid = groupUid,
            newExpense = newExpense
          )
        }
      )
    } yield e
  }

  private def validateRequestData(
    data: PutGroupRequest
  ): IO[DomainError, Unit] = {
    if (data.members.isDefined) {
      val members = data.members.getOrElse(List.empty)
      if (members.size < 2) {
        return ZIO.fail(DomainError(message = "At least 2 members should be specified".some))
      }
    }

    ZIO.succeed(())
  }

  private def validateRequestData(data: PostGroupRequest): IO[DomainError, Unit] = {
    val members = data.members.getOrElse(List.empty)
    val expenses = data.expenses.getOrElse(List.empty)
    val memberNames = members.map(member => member.name).toSet

    for {
      _ <- ZIO.collectAll(members.map(member => isValidMember(member)))

      _ <- if (members.size > 1 && members.size < 2) {
        ZIO.fail(DomainError(message = "At least 2 members should be specified".some))
      } else {
        ZIO.succeed(())
      }

      _ <- ZIO.collectAll(expenses.map(expense => isValidExpense(expense, memberNames)))
    } yield ()
  }

  private def isValidMember(member: UserNameDto): IO[DomainError, Unit] = {
    for {
      _ <- if (member.name.isBlank) {
        ZIO.fail(DomainError(message = "Invalid member name is empty".some))
      } else {
        ZIO.succeed(())
      }

    } yield ()
  }

  private def isValidExpense(
    expense: NewExpenseDto,
    memberNames: Set[String]
  ): IO[DomainError, Unit] = {
    // TODO: check amount is > 0
    val splitBetween = expense.splitBetween.getOrElse(List.empty)
    val isSplitBetweenAll = expense.isSplitBetweenAll.getOrElse(false)

    for {
      _ <- if (expense.title.isBlank) {
        ZIO.fail(DomainError(message = "Expense title is empty".some))
      } else {
        ZIO.succeed(())
      }

      _ <- ZIO.collectAll(
        expense.paidBy.map { payer =>
          if (memberNames.contains(payer.name)) {
            ZIO.succeed(())
          } else {
            ZIO.fail(DomainError(message = s"Payer is not a member of the group: ${payer.name}".some))
          }
        }
      )

      _ <- ZIO.collectAll(
        expense.splitBetween.getOrElse(List.empty)
          .map { splitee =>
            if (memberNames.contains(splitee.name)) {
              ZIO.succeed(())
            } else {
              ZIO.fail(DomainError(message = s"Cannot split to not a member: ${splitee.name}".some))
            }
          }
      )

      _ <- if (isSplitBetweenAll && splitBetween.nonEmpty) {
        ZIO.fail(DomainError(message = "Invalid split option".some))
      } else if (!isSplitBetweenAll && splitBetween.isEmpty) {
        ZIO.fail(DomainError(message = "Invalid split option".some))
      } else {
        ZIO.succeed(())
      }
    } yield ()
  }

  private def parseData(request: Request): IO[DomainError, PostGroupRequest] = {
    for {
      data <- request.body.parse[PostGroupRequest]
    } yield data
  }

  private def parseUids(request: Request): IO[DomainError, List[UUID]] = {
    for {
      uids <- {
        val uids = request.url.queryParamOrElse("ids", "")
          .split(",")
          .toList
          .map(id => id.asUid())

        if (uids.nonEmpty) {
          ZIO.collectAll(uids)
        } else {
          ZIO.fail(DomainError(message = "No group ids were specified".some))
        }
      }
    } yield uids
  }

  private def parsePasswords(request: Request): IO[DomainError, List[String]] = {
    val passwords = request.url.queryParamOrElse("passwords", "")
      .split(",")
      .toList

    ZIO.succeed(passwords)
  }
}
