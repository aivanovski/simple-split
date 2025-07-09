package com.github.ai.split.entity.db

import java.util.UUID

case class PaidByEntity(
  groupUid: UUID,
  expenseUid: UUID,
  userUid: UUID
)
