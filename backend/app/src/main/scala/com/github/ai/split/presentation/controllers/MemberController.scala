package com.github.ai.split.presentation.controllers

import com.github.ai.split.domain.usecases.{
  AddMembersUseCase,
  AssembleGroupResponseUseCase,
  GetGroupUseCase,
  RemoveMembersUseCase,
  UpdateMemberUseCase
}
import com.github.ai.split.api.request.{PostMemberRequest, PutMemberRequest}
import com.github.ai.split.api.response.{DeleteMemberResponse, PostMemberResponse, PutMemberResponse}
import com.github.ai.split.data.db.model.{GroupUid, MemberUid}
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.utils.{parsePasswordParam, parseUid, parseUidFromUrl}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.http.{Request, Response}
import zio.direct.*

class MemberController(
  private val accessResolver: AccessResolverService,
  private val accessResolverService: AccessResolverService,
  private val getGroupUseCase: GetGroupUseCase,
  private val addMemberUseCase: AddMembersUseCase,
  private val removeMembersUseCase: RemoveMembersUseCase,
  private val updateMemberUseCase: UpdateMemberUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase
) {

  def createMember(
    password: String,
    body: PostMemberRequest
  ): IO[DomainError, PostMemberResponse] =
    defer {
      val groupUid = GroupUid(body.groupUid.parseUid().run)
      accessResolverService.canAccessToGroup(groupUid = groupUid, password = password).run

      addMemberUseCase.addMember(
        groupUid = groupUid,
        name = body.name
      ).run

      val groupDto = assembleGroupUseCase.assembleGroupDto(groupUid).run
      PostMemberResponse(groupDto)
    }

  def updateMember(
    memberId: String,
    password: String,
    body: PutMemberRequest
  ): IO[DomainError, PutMemberResponse] =
    defer {
      val memberUid = memberId.parseUid().map(uid => MemberUid(uid)).run

      accessResolver.canAccessToMember(memberUid = memberUid, password = password).run

      val member = updateMemberUseCase.updateMember(memberUid = memberUid, newName = body.name).run

      val groupDto = assembleGroupUseCase.assembleGroupDto(groupUid = member.groupUid).run
      PutMemberResponse(groupDto)
    }

  def removeMember(
    memberId: String,
    password: String
  ): IO[DomainError, DeleteMemberResponse] =
    defer {
      val memberUid = MemberUid(memberId.parseUid().run)
      accessResolverService.canAccessToMember(memberUid = memberUid, password = password).run

      val group = getGroupUseCase.getGroupByMemberUid(memberUid).run

      removeMembersUseCase.removeMemberByUids(memberUids = List(memberUid)).run

      val groupDto = assembleGroupUseCase.assembleGroupDto(groupUid = group.uid).run
      DeleteMemberResponse(groupDto)
    }
}
