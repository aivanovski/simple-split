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
  SplitBetweenMembers,
  UserReference
}
import com.github.ai.split.api.request.{PostGroupRequest, PutGroupRequest}
import com.github.ai.split.api.response.{GetGroupsResponse, PostGroupResponse, PutGroupResponse}
import com.github.ai.split.entity.Access.{DENIED, GRANTED}
import com.github.ai.split.entity.FileExtension.{CSV, HTML}
import com.github.ai.split.entity.db.{ExpenseEntity, GroupMemberEntity, GroupUid, UserEntity, UserUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{getLastUrlParameter, parse, parsePasswordParam, parseUid, parseUidFromUrl, some}
import zio.{IO, ZIO}
import zio.http.{Body, Charsets, Header, Headers, MediaType, Request, Response, Status}
import zio.json.*
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
    request: Request
  ): IO[DomainError, Response] = {
    for {
      groupUids <- parseUids(request).map(uids => uids.map(GroupUid(_)))
      passwords <- parsePasswords(request)
      uidsAndAccesses <- accessResolver.canAccessToGroups(groupUids = groupUids, passwords = passwords)

      grantedGroupsUids = uidsAndAccesses
        .filter(result => result.access == GRANTED)
        .map(result => result.uid)

      deniedGroupUids = uidsAndAccesses
        .filter(result => result.access == DENIED)
        .map(result => result.uid)

      groups <- assembleGroupsUseCase.assembleGroupDtos(uids = grantedGroupsUids)
      errors <- ZIO
        .succeed(deniedGroupUids.map { uid =>
          GetGroupErrorDto(
            uid = uid.toString,
            message = "Not found"
          )
        })
    } yield Response.json(GetGroupsResponse(groups, errors).toJsonPretty)
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
          ZIO
            .collectAll(
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
        newCurrencyIsoCode = data.currencyIsoCode.map(_.trim).filter(_.nonEmpty),
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
        val newUsers = data.members
          .getOrElse(List.empty)
          .map(member => NewUser(name = member.name))

        addGroupUseCase.addGroup(
          NewGroup(
            password = data.password.trim,
            title = data.title.trim,
            description = data.description.getOrElse(""),
            currencyIsoCode = data.currencyIsoCode.trim,
            members = newUsers,
            expenses = newExpenses
          )
        )
      }

      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid = newGroup.uid)
    } yield Response.json(PostGroupResponse(groupDto).toJsonPretty)
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
        .getOrElse(List.empty)
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
        val uids = request.url
          .queryParamOrElse("ids", "")
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
    val passwords = request.url
      .queryParamOrElse("passwords", "")
      .split(",")
      .toList

    ZIO.succeed(passwords)
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
