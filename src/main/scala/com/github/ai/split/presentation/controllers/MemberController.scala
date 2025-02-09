package com.github.ai.split.presentation.controllers

import com.github.ai.split.data.{GroupRepository, UserRepository}
import com.github.ai.split.entity.User
import com.github.ai.split.entity.api.request.PostMemberRequest
import com.github.ai.split.utils.some
import com.github.ai.split.utils.parse
import com.github.ai.split.utils.asUid
import com.github.ai.split.utils.toGroupDto
import com.github.ai.split.entity.exception.DomainError
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

class MemberController(
  private val groupRepository: GroupRepository,
  private val userRepository: UserRepository
) {

  def postMember(
    user: User,
    groupId: String,
    request: Request
  ): ZIO[Any, DomainError, Response] = {
    for
      body <- request.body.parse[PostMemberRequest]
      groupUid <- groupId.asUid()
      member <- userRepository.getByEmail(body.email)
      group <- groupRepository.getByUid(groupUid)
      updatedGroup <- {
        if (group.members.contains(member.uid)) {
          ZIO.fail(new DomainError(message = "Member already exists".some))
        } else {
          val newMembers = group.members :+ member.uid
          val newGroup = group.copy(members = newMembers)
          groupRepository.updateGroup(newGroup)
        }
      }

      userUidToUserMap <- userRepository.getUserUidToUserMap()

      response <- {
        toGroupDto(
          group = updatedGroup,
          userUidToUserMap = userUidToUserMap
        )
      }
    yield
      Response.text(response.toJsonPretty + "\n")
  }
}
