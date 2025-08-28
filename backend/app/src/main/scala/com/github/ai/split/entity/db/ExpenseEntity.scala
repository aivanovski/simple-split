package com.github.ai.split.entity.db

import java.time.LocalDateTime

case class ExpenseEntity(
  uid: ExpenseUid,
  groupUid: GroupUid,
  title: String,
  description: String,
  amount: Double,
  isSplitBetweenAll: Boolean,
  created: LocalDateTime,
  modified: LocalDateTime
)
