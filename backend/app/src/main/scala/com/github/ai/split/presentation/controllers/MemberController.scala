package com.github.ai.split.presentation.controllers

import com.github.ai.split.domain.usecases.{AddMemberUseCase, AssembleGroupResponseUseCase, GetGroupByUidUseCase}
import com.github.ai.split.api.request.PostMemberRequest
import com.github.ai.split.api.response.PostMemberResponse
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.utils.{asUid, getLastUrlParameter, parse, parsePasswordParam, parseUidFromUrl}
import com.github.ai.split.entity.exception.DomainError
import zio.{IO, ZIO}
import zio.http.{Request, Response, boolean}
import zio.json.*

class MemberController(
  private val accessResolver: AccessResolverService,
  private val accessResolverService: AccessResolverService,
  private val getGroupByUidUseCase: GetGroupByUidUseCase,
  private val addMemberUseCase: AddMemberUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase
) {

  def postMember(
    request: Request
  ): ZIO[Any, DomainError, Response] = {
    for {
      groupUid <- parseUidFromUrl(request)
      password <- parsePasswordParam(request)
      _ <- accessResolverService.canAccessToGroup(groupUid = groupUid, password = password)
      body <- request.body.parse[PostMemberRequest]
      memberUid <- body.uid.asUid()
      group <- getGroupByUidUseCase.getGroupByUid(groupUid)
      newMember <- addMemberUseCase.addMember(
        groupUid = groupUid,
        userUid = memberUid
      )
      groupDto <- assembleGroupUseCase.assembleGroupDto(groupUid)
    } yield Response.json(PostMemberResponse(groupDto).toJsonPretty)
  }
}