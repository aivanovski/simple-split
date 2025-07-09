package com.github.ai.split.entity.db

import java.util.UUID

case class SplitBetweenEntity(
  groupUid: UUID,
  expenseUid: UUID,
  userUid: UUID
)
