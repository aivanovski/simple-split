package com.github.ai.split.presentation.controllers

import com.github.ai.split.data.{GroupRepository, UserRepository}
import com.github.ai.split.entity.{Group, User}
import com.github.ai.split.entity.api.GroupDto
import com.github.ai.split.entity.api.request.{PostGroupRequest, PostUserRequest}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{toGroupDto, toGroupDtos, some, parse}
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

import java.util.UUID

class GroupController(
  private val groupRepository: GroupRepository,
  private val userRepository: UserRepository
) {

  def getGroups(request: Request): ZIO[Any, DomainError, Response] = {
    for
      groups <- groupRepository.getGroups()
      userUidToUserMap <- userRepository.getUserUidToUserMap()
      response <- toGroupDtos(groups, userUidToUserMap)
    yield
      Response.text(response.toJsonPretty + "\n")
  }

  def postGroup(user: User, request: Request): ZIO[Any, DomainError, Response] = {
    for
      data <- request.body.parse[PostGroupRequest]
      group <- groupRepository.add(createGroup(user, data))
      userUidToUserMap <- userRepository.getUserUidToUserMap()
      response <- toGroupDto(group, userUidToUserMap)
    yield
      Response.text(response.toJsonPretty + "\n")
  }

  private def createGroup(
    user: User,
    request: PostGroupRequest
  ): Group =
    Group(
      ownerUid = user.uid,
      title = request.title,
      description = request.description.getOrElse(""),
      members = List.empty,
      expenses = List.empty
    )
}
