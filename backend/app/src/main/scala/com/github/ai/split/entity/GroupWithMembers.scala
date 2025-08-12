package com.github.ai.split.entity

import com.github.ai.split.entity.db.GroupEntity

case class GroupWithMembers(
  entity: GroupEntity,
  members: List[Member]
)
