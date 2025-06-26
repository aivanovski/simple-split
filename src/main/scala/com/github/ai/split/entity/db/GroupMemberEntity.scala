package com.github.ai.split.entity.db

import java.util.UUID

case class GroupMemberEntity(
  groupUid: UUID,
  userUid: UUID
)
