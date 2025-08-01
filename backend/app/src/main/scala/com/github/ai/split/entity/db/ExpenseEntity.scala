package com.github.ai.split.entity.db

case class ExpenseEntity(
  uid: ExpenseUid,
  groupUid: GroupUid,
  title: String,
  description: String,
  amount: Double,
  isSplitBetweenAll: Boolean
)