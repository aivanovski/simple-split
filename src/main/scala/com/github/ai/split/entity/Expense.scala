package com.github.ai.split.entity

import java.util.UUID

case class Expense(
  uid: UUID,
  paidBy: List[PaymentTransaction],
  splitBetween: List[UUID]
)
