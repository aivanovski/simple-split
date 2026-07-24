package com.github.ai.split.data.db.model

case class ExpenseEntity(
  uid: ExpenseUid,
  groupUid: GroupUid,
  title: String,
  description: String,
  amount: Double,
  isSplitBetweenAll: Boolean,
  created: Timestamp,
  modified: Timestamp
)
