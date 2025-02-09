package com.github.ai.split.entity

import java.util.UUID

case class Expense(
  uid: UUID,
  groupUid: UUID,
  title: String,
  description: String,
  amount: Double,
  paidBy: List[UUID],
  splitBetween: List[UUID]
)
