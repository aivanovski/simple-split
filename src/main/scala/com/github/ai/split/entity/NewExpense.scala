package com.github.ai.split.entity

import java.util.UUID

case class NewExpense(
  title: String,
  description: String,
  amount: Double,
  paidBy: List[UUID],
  splitBetween: List[UUID]
)
