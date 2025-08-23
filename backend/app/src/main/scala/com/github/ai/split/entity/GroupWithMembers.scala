package com.github.ai.split.entity

import com.github.ai.split.entity.db.{CurrencyEntity, GroupEntity}

case class GroupWithMembers(
  entity: GroupEntity,
  currency: CurrencyEntity,
  members: List[Member]
)
