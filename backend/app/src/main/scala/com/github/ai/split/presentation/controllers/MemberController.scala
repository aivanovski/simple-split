package com.github.ai.split.presentation.controllers

import com.github.ai.split.domain.usecases.{
  AddMembersUseCase,
  AddUserUseCase,
  AssembleGroupResponseUseCase,
  GetGroupUseCase,
  RemoveMembersUseCase,
  UpdateMemberUseCase
}
import com.github.ai.split.api.request.{PostMemberRequest, PutMemberRequest}
import com.github.ai.split.api.response.{DeleteMemberResponse, PostMemberResponse, PutMemberResponse}
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.entity.db.{GroupUid, MemberUid}
import com.github.ai.split.utils.{parse, parsePasswordParam, parseUid, parseUidFromUrl}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.http.{Request, Response}
import zio.json.*
import zio.direct.*

class MemberController(
  private val accessResolver: AccessResolverService,
  private val accessResolverService: AccessResolverService,
  private val getGroupUseCase: GetGroupUseCase,
  private val addUserUseCase: AddUserUseCase,
  private val addMemberUseCase: AddMembersUseCase,
  private val removeMembersUseCase: RemoveMembersUseCase,
  private val updateMemberUseCase: UpdateMemberUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase
) {

  def createMember(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      password <- parsePasswordParam(request)
      body <- request.body.parse[PostMemberRequest]
      groupUid <- body.groupUid.parseUid().map(uid => GroupUid(uid))
      _ <- accessResolverService.canAccessToGroup(groupUid = groupUid, password = password)

      newMember <- addMemberUseCase.addMember(
        groupUid = groupUid,
        name = body.name
      )
      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid)
    } yield Response.json(PostMemberResponse(groupDto).toJsonPretty)
  }

  def updateMember(
    request: Request
  ): IO[DomainError, Response] = {
    defer {
      val password = parsePasswordParam(request).run
      val memberUid = parseUidFromUrl(request).map(uid => MemberUid(uid)).run
      val body = request.body.parse[PutMemberRequest].run
      accessResolver.canAccessToMember(memberUid = memberUid, password = password).run

      val member = updateMemberUseCase.updateMember(memberUid = memberUid, newName = body.name).run

      val groupDto = assembleGroupUseCase.assembleGroupDto(groupUid = member.groupUid).run
      Response.json(PutMemberResponse(groupDto).toJsonPretty)
    }
  }

  def removeMember(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      password <- parsePasswordParam(request)
      memberUid <- parseUidFromUrl(request).map(uid => MemberUid(uid))
      _ <- accessResolverService.canAccessToMember(memberUid = memberUid, password = password)

      group <- getGroupUseCase.getGroupByMemberUid(memberUid)

      _ <- removeMembersUseCase.removeMemberByUids(memberUids = List(memberUid))

      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid = group.uid)
    } yield Response.json(DeleteMemberResponse(groupDto).toJsonPretty)
  }
}
