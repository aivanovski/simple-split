package com.github.ai.split.entity.db

import java.util.UUID

case class ExpenseEntity(
  uid: UUID,
  groupUid: UUID,
  title: String,
  description: String,
  amount: Double,
  isSplitBetweenAll: Boolean
)
