package com.github.ai.split.presentation.controllers

import com.github.ai.split.domain.usecases.{AddMemberUseCase, AssembleGroupResponseUseCase, GetAllUsersUseCase, GetGroupByUidUseCase, GetUserByEmailUseCase}
import com.github.ai.split.entity.api.request.PostMemberRequest
import com.github.ai.split.entity.db.UserEntity
import com.github.ai.split.utils.parse
import com.github.ai.split.utils.asUid
import com.github.ai.split.utils.getLastUrlParameter
import com.github.ai.split.entity.exception.DomainError
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

class MemberController(
  private val getGroupByUidUseCase: GetGroupByUidUseCase,
  private val getUserByEmailUseCase: GetUserByEmailUseCase,
  private val getAllUsersUseCase: GetAllUsersUseCase,
  private val addMemberUseCase: AddMemberUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase
) {

  def postMember(
    user: UserEntity,
    request: Request
  ): ZIO[Any, DomainError, Response] = {
    for {
      body <- request.body.parse[PostMemberRequest]
      groupUidStr <- request.getLastUrlParameter()
      groupUid <- groupUidStr.asUid()
      memberUser <- getUserByEmailUseCase.getUserByEmail(body.email)
      group <- getGroupByUidUseCase.getGroupByUid(groupUid)
      newMember <- addMemberUseCase.addMember(
        groupUid = groupUid,
        memberUserUid = memberUser.uid
      )
      response <- assembleGroupUseCase.assembleGroupDto(groupUid)
    } yield Response.text(response.toJsonPretty + "\n")
  }
}