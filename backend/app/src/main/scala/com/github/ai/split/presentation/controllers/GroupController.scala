package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.{GetGroupErrorDto, NewExpenseDto, UserNameDto}
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.domain.usecases.{
  AddExpenseUseCase,
  AddGroupUseCase,
  AddMembersUseCase,
  AddUserUseCase,
  AssembleGroupResponseUseCase,
  AssembleGroupsResponseUseCase,
  ExportGroupDataUseCase,
  GetAllUsersUseCase,
  UpdateGroupUseCase
}
import com.github.ai.split.entity.{
  FileExtension,
  NameReference,
  NewExpense,
  NewGroup,
  NewUser,
  SplitBetweenAll,
  SplitBetweenMembers
}
import com.github.ai.split.api.request.{PostGroupRequest, PutGroupRequest}
import com.github.ai.split.api.response.{GetGroupsResponse, PostGroupResponse, PutGroupResponse}
import com.github.ai.split.data.db.model.{GroupUid, MemberUid}
import com.github.ai.split.entity.Access.{DENIED, GRANTED}
import com.github.ai.split.entity.FileExtension.{CSV, HTML}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{getLastUrlParameter, parsePasswordParam, parseUid, parseUidFromUrl, some}
import zio.{IO, ZIO}
import zio.http.{Body, Charsets, Header, Headers, MediaType, Request, Response, Status}
import zio.direct.*

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
  private val updateGroupUseCase: UpdateGroupUseCase,
  private val exportDataUseCase: ExportGroupDataUseCase
) {

  def getGroups(
    ids: String,
    passwordValues: String
  ): IO[DomainError, GetGroupsResponse] = {
    for {
      groupUids <- parseUids(ids).map(uids => uids.map(GroupUid(_)))
      passwords = passwordValues.split(",").toList
      uidsAndAccesses <- accessResolver.canAccessToGroups(groupUids = groupUids, passwords = passwords)

      grantedGroupsUids = uidsAndAccesses
        .filter(result => result.access == GRANTED)
        .map(result => result.uid)

      deniedGroupUids = uidsAndAccesses
        .filter(result => result.access == DENIED)
        .map(result => result.uid)

      groups <- assembleGroupsUseCase.assembleGroupDtos(uids = grantedGroupsUids)
      errors <- ZIO
        .succeed(
          deniedGroupUids.map { uid =>
            GetGroupErrorDto(
              uid.toString,
              "Not found"
            )
          }
        )
    } yield GetGroupsResponse(groups, errors)
  }

  def updateGroup(
    groupId: String,
    password: String,
    data: PutGroupRequest
  ): IO[DomainError, PutGroupResponse] = {
    for {
      groupUid <- groupId.parseUid().map(uid => GroupUid(uid))
      _ <- accessResolver.canAccessToGroup(groupUid = groupUid, password = password)

      newMembers <- {
        val newMembers = data.members
        if (newMembers.nonEmpty) {
          ZIO
            .collectAll(
              newMembers.map(member => member.uid.parseUid().map(uid => MemberUid(uid)))
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
        newCurrencyIsoCode = data.currencyIsoCode.map(_.trim).filter(_.nonEmpty),
        newMemberUids = newMembers
      )

      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid = groupUid)
    } yield PutGroupResponse(groupDto)
  }

  def createGroup(
    data: PostGroupRequest
  ): IO[DomainError, PostGroupResponse] = {
    for {
      newExpenses <- parseNewExpenses(
        expenses = data.expenses
      )

      newGroup <- {
        val newUsers = data.members.map(member => NewUser(name = member.name))

        addGroupUseCase.addGroup(
          NewGroup(
            password = data.password.trim,
            title = data.title.trim,
            description = data.description.trim,
            currencyIsoCode = data.currencyIsoCode.trim,
            members = newUsers,
            expenses = newExpenses
          )
        )
      }

      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid = newGroup.uid)
    } yield PostGroupResponse(groupDto)
  }

  def exportGroup(
    request: Request
  ): IO[DomainError, Response] = {
    defer {
      val password = parsePasswordParam(request).run
      val (groupUid, extension) = parseGroupUidAndExtension(request).run
      accessResolver.canAccessToGroup(groupUid = groupUid, password = password).run

      val data = extension match
        case CSV => exportDataUseCase.exportDataToCsv(groupUid).run
        // TODO: implement for HTML
        case HTML => ZIO.fail(DomainError(message = "Invalid file format requested".some)).run

      val headers = extension match
        case CSV =>
          List(
            Header.Custom(MediaType.text.csv.mainType, Charsets.Utf8.name()),
            Header.Custom("Content-Disposition", s"attachment; filename=\"${data.fileName}\"")
          )
        case HTML =>
          List(
            Header.Custom(MediaType.text.html.mainType, Charsets.Utf8.name())
          )

      Response(
        status = Status.Ok,
        headers = Headers(headers),
        body = Body.fromString(data.content)
      )
    }
  }

  private def parseNewExpenses(
    expenses: List[NewExpenseDto]
  ): IO[DomainError, List[NewExpense]] = {
    val newExpenses = expenses.map { expense =>
      val isSplitBetweenAll = expense.isSplitBetweenAll.getOrElse(true)
      val splitMembers = expense.splitBetween
        .map(splitMember => NameReference(name = splitMember.name))

      NewExpense(
        title = expense.title,
        description = expense.description,
        amount = expense.amount,
        paidBy = expense.paidBy.map(payer => NameReference(name = payer.name)),
        split = if (isSplitBetweenAll) SplitBetweenAll else SplitBetweenMembers(splitMembers)
      )
    }

    ZIO.succeed(newExpenses)
  }

  private def parseUids(ids: String): IO[DomainError, List[UUID]] = {
    for {
      uids <- {
        val uids = ids
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

  private def parseGroupUidAndExtension(
    request: Request
  ): IO[DomainError, (GroupUid, FileExtension)] = {
    for {
      text <- request.getLastUrlParameter()

      values = text.split("\\.").toList

      _ <-
        if (values.size != 2) {
          ZIO.fail(DomainError(message = "Invalid url".some))
        } else {
          ZIO.succeed(())
        }

      uid <- values.head.parseUid()

      extensionStr = values(1)

      extension <- ZIO
        .fromOption(
          FileExtension.fromString(extensionStr.toUpperCase)
        )
        .mapError(_ => DomainError(message = s"Invalid extension: $extensionStr".some))
    } yield (GroupUid(uid), extension)
  }
}
