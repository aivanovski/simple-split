package com.github.ai.split.presentation.controllers

import com.github.ai.split.domain.usecases.{AddGroupUseCase, AssembleGroupResponseUseCase, AssembleGroupsResponseUseCase, GetAllUsersUseCase}
import com.github.ai.split.entity.api.GroupDto
import com.github.ai.split.entity.api.request.{PostGroupRequest, PostUserRequest}
import com.github.ai.split.entity.db.{GroupEntity, UserEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{parse, some}
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

import java.util.UUID

class GroupController(
  private val addGroupUseCase: AddGroupUseCase,
  private val getAllUsersUseCase: GetAllUsersUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase,
  private val assembleGroupsUseCase: AssembleGroupsResponseUseCase
) {

  def getGroups(user: UserEntity): ZIO[Any, DomainError, Response] = {
    for {
      response <- assembleGroupsUseCase.assembleGroupDtos(userUid = user.uid)
    } yield Response.text(response.toJsonPretty + "\n")
  }

  def postGroup(user: UserEntity, request: Request): ZIO[Any, DomainError, Response] = {
    for {
      requestData <- request.body.parse[PostGroupRequest]
      group <- addGroupUseCase.addGroup(createNewGroup(user, requestData))
      response <- assembleGroupUseCase.assembleGroupDto(groupUid = group.uid)
    } yield Response.text(response.toJsonPretty + "\n")
  }

  private def createNewGroup(
    user: UserEntity,
    requestData: PostGroupRequest
  ): GroupEntity =
    GroupEntity(
      uid = UUID.randomUUID(),
      ownerUid = user.uid,
      title = requestData.title,
      description = requestData.description.getOrElse("")
    )
}
