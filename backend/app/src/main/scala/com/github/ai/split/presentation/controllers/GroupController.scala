package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.{GetGroupErrorDto, NewExpenseDto, UserNameDto}
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.domain.usecases.{
  AddExpenseUseCase,
  AddGroupUseCase,
  AddMembersUseCase,
  AssembleGroupResponseUseCase,
  AssembleGroupsResponseUseCase,
  ExportGroupDataUseCase,
  UpdateGroupUseCase
}
import com.github.ai.split.entity.{
  FileExtension,
  NameReference,
  NewExpense,
  NewGroup,
  NewMember,
  SplitBetweenAll,
  SplitBetweenMembers
}
import com.github.ai.split.api.request.{PostGroupRequest, PutGroupRequest}
import com.github.ai.split.api.response.{GetGroupsResponse, PostGroupResponse, PutGroupResponse}
import com.github.ai.split.data.db.model.{GroupUid, MemberUid, UserEntity}
import com.github.ai.split.data.db.repository.GroupRepository
import com.github.ai.split.entity.Access.GRANTED
import com.github.ai.split.entity.FileExtension.{CSV, HTML}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{getLastUrlParameter, parseUid, some}
import zio.{IO, ZIO}
import zio.http.{Body, Charsets, Header, Headers, MediaType, Request, Response, Status}
import zio.direct.*

import java.util.UUID

class GroupController(
  private val accessResolver: AccessResolverService,
  private val addMemberUseCase: AddMembersUseCase,
  private val addGroupUseCase: AddGroupUseCase,
  private val addExpenseUseCase: AddExpenseUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase,
  private val assembleGroupsUseCase: AssembleGroupsResponseUseCase,
  private val updateGroupUseCase: UpdateGroupUseCase,
  private val exportDataUseCase: ExportGroupDataUseCase,
  private val groupRepository: GroupRepository
) {

  def getGroups(
    user: UserEntity,
    ids: Option[String]
  ): IO[DomainError, GetGroupsResponse] =
    defer {
      val (grantedGroupUids, deniedGroupUids) = ids match {
        case Some(value) =>
          val groupUids = parseUids(value).run.map(GroupUid(_))

          val uidsAndAccesses = accessResolver
            .canAccessToGroups(userUid = user.uid, groupUids = groupUids)
            .run

          uidsAndAccesses.partition(_.access == GRANTED) match {
            case (granted, denied) => (granted.map(_.uid), denied.map(_.uid))
          }
        case None =>
          (groupRepository.getUserGroupUids(user.uid).run, List.empty)
      }

      val groups = assembleGroupsUseCase.assembleGroupDtos(uids = grantedGroupUids).run
      val errors = deniedGroupUids.map { uid =>
        GetGroupErrorDto(
          uid.toString,
          "Not found"
        )
      }

      GetGroupsResponse(groups, errors)
    }

  def updateGroup(
    user: UserEntity,
    groupId: String,
    data: PutGroupRequest
  ): IO[DomainError, PutGroupResponse] =
    defer {
      val groupUid = GroupUid(groupId.parseUid().run)
      accessResolver.canAccessToGroup(userUid = user.uid, groupUid = groupUid).run

      val members = data.members
      val newMembers =
        if (members.nonEmpty) {
          Some(
            ZIO
              .foreach(members) { member =>
                member.uid.parseUid().map(uid => MemberUid(uid))
              }
              .run
          )
        } else {
          None
        }

      updateGroupUseCase
        .updateGroup(
          groupUid = groupUid,
          newPassword = data.password.map(_.trim).filter(_.nonEmpty),
          newTitle = data.title.map(_.trim).filter(_.nonEmpty),
          newDescription = data.description.map(_.trim).filter(_.nonEmpty),
          newCurrencyIsoCode = data.currencyIsoCode.map(_.trim).filter(_.nonEmpty),
          newMemberUids = newMembers
        )
        .run

      val groupDto = assembleGroupUseCase.assembleGroupDto(groupUid = groupUid).run
      PutGroupResponse(groupDto)
    }

  def createGroup(
    user: UserEntity,
    data: PostGroupRequest
  ): IO[DomainError, PostGroupResponse] =
    defer {
      val newExpenses = parseNewExpenses(
        expenses = data.expenses
      ).run

      val newUsers = data.members
        .filterNot(member => member.name.trim.equalsIgnoreCase(user.name.trim))
        .map(member => NewMember(name = member.name))

      val newGroup = addGroupUseCase
        .addGroup(
          NewGroup(
            password = data.password.trim,
            title = data.title.trim,
            description = data.description.trim,
            currencyIsoCode = data.currencyIsoCode.trim,
            members = newUsers,
            expenses = newExpenses
          )
        )
        .run

      addMemberUseCase.addMembers(groupUid = newGroup.uid, userUids = List(user.uid)).run

      val groupDto = assembleGroupUseCase.assembleGroupDto(groupUid = newGroup.uid).run
      PostGroupResponse(groupDto)
    }

  def exportGroup(
    user: UserEntity,
    request: Request
  ): IO[DomainError, Response] =
    defer {
      val (groupUid, extension) = parseGroupUidAndExtension(request).run
      accessResolver.canAccessToGroup(userUid = user.uid, groupUid = groupUid).run

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

  private def parseNewExpenses(
    expenses: List[NewExpenseDto]
  ): IO[DomainError, List[NewExpense]] =
    defer {
      expenses.map { expense =>
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
    }

  private def parseUids(ids: String): IO[DomainError, List[UUID]] = {
    defer {
      val values = ZIO.succeed(ids.split(",").toList).run

      if (values.nonEmpty) {
        ZIO.foreach(values)(_.parseUid()).run
      } else {
        ZIO.fail(DomainError(message = "No group ids were specified".some)).run
      }
    }
  }

  private def parseGroupUidAndExtension(
    request: Request
  ): IO[DomainError, (GroupUid, FileExtension)] =
    defer {
      val text = request.getLastUrlParameter().run
      val values = ZIO.succeed(text.split("\\.").toList).run

      if (values.size != 2) {
        ZIO.fail(DomainError(message = "Invalid url".some)).run
      }

      val uid = values.head.parseUid().run
      val extensionStr = values(1)
      val extension = ZIO
        .fromOption(
          FileExtension.fromString(extensionStr.toUpperCase)
        )
        .mapError(_ => DomainError(message = s"Invalid extension: $extensionStr".some))
        .run

      (GroupUid(uid), extension)
    }
}
