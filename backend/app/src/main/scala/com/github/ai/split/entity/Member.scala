package com.github.ai.split.entity

import com.github.ai.split.entity.db.{GroupMemberEntity, UserEntity}

case class Member(
  user: UserEntity,
  entity: GroupMemberEntity
)
