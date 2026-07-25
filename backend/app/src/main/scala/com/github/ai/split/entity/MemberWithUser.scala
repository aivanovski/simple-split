package com.github.ai.split.entity

import com.github.ai.split.data.db.model.{MemberEntity, UserEntity}

case class MemberWithUser(
  member: MemberEntity,
  user: Option[UserEntity]
) {

  def getName(): String = {
    val userName = user.map(_.name)

    member.name
      .orElse(userName)
      .orElse(member.email)
      .getOrElse("")
  }

}
