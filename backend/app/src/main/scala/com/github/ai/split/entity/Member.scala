package com.github.ai.split.entity

import com.github.ai.split.entity.db.{GroupMembershipEntity, MemberEntity}

case class Member(
  user: MemberEntity,
  entity: GroupMembershipEntity
)
