package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.{NewExpenseDto, UserNameDto}
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.domain.usecases.{AddExpenseUseCase, AddGroupUseCase, AddMembersUseCase, AddUserUseCase, AssembleGroupResponseUseCase, AssembleGroupsResponseUseCase, GetAllUsersUseCase, UpdateGroupUseCase}
import com.github.ai.split.entity.{Member, NameReference, NewExpense, NewGroup, NewUser, Split, SplitBetweenAll, SplitBetweenMembers, UserReference}
import com.github.ai.split.api.request.{PostGroupRequest, PutGroupRequest}
import com.github.ai.split.api.response.{GetGroupsResponse, PostGroupResponse, PutGroupResponse}
import com.github.ai.split.data.db.dao.GroupMemberEntityDao
import com.github.ai.split.entity.db.{ExpenseEntity, GroupMemberEntity, GroupUid, UserEntity, UserUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{parse, parsePasswordParam, parseUid, parseUidFromUrl, some}
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

import java.util.UUID

class GroupController(
  private val accessResolver: AccessResolverService,
  private val addUserUseCase: AddUserUseCase,
  private val addMemberUseCase: AddMembersUseCase,
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
      groupUids <- parseUids(request).map(uids => uids.map(GroupUid(_)))
      passwords <- parsePasswords(request)
      _ <- accessResolver.canAccessToGroups(groupUids = groupUids, passwords = passwords)

      groups <- assembleGroupsUseCase.assembleGroupDtos(uids = groupUids)
    } yield Response.json(GetGroupsResponse(groups).toJsonPretty)
  }

  def updateGroup(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      groupUid <- parseUidFromUrl(request).map(uid => GroupUid(uid))
      password <- parsePasswordParam(request)
      _ <- accessResolver.canAccessToGroup(groupUid = groupUid, password = password)

      data <- request.body.parse[PutGroupRequest]
      newMembers <- {
        val newMembers = data.members.getOrElse(List.empty)
        if (newMembers.nonEmpty) {
          ZIO.collectAll(
              newMembers.map(member => member.uid.parseUid().map(uid => UserUid(uid)))
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
    } yield Response.json(PutGroupResponse(groupDto).toJsonPretty)
  }

  def createGroup(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      data <- request.body.parse[PostGroupRequest]

      newExpenses <- parseNewExpenses(
        expenses = data.expenses.getOrElse(List.empty)
      )

      newGroup <- {
        val newUsers = data.members.getOrElse(List.empty)
          .map(member => NewUser(name = member.name))

        addGroupUseCase.addGroup(
          NewGroup(
            password = data.password,
            title = data.title,
            description = data.description.getOrElse(""),
            members = newUsers,
            expenses = newExpenses
          )
        )
      }

      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid = newGroup.uid)
    } yield Response.json(PostGroupResponse(groupDto).toJsonPretty)
  }

  private def parseNewExpenses(
    expenses: List[NewExpenseDto]
  ): IO[DomainError, List[NewExpense]] = {
    val newExpenses = expenses.map { expense =>
      val isSplitBetweenAll = expense.isSplitBetweenAll.getOrElse(true)
      val splitMembers = expense.splitBetween.getOrElse(List.empty)
        .map(splitMember => NameReference(name = splitMember.name))

      NewExpense(
        title = expense.title,
        description = expense.description.getOrElse(""),
        amount = expense.amount,
        paidBy = expense.paidBy.map(payer => NameReference(name = payer.name)),
        split = if (isSplitBetweenAll) SplitBetweenAll else SplitBetweenMembers(splitMembers)
      )
    }

    ZIO.succeed(newExpenses)
  }

  private def parseUids(request: Request): IO[DomainError, List[UUID]] = {
    for {
      uids <- {
        val uids = request.url.queryParamOrElse("ids", "")
          .split(",")
          .toList
          .map(id => id.parseUid())

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
