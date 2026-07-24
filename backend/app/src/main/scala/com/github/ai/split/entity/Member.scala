package com.github.ai.split.entity

import com.github.ai.split.data.db.model.{GroupMembershipEntity, MemberEntity}

case class Member(
  user: MemberEntity,
  entity: GroupMembershipEntity
)
