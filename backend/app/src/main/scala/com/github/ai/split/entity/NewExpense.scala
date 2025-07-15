package com.github.ai.split.entity

import java.util.UUID

case class NewExpense(
  title: String,
  description: String,
  amount: Double,
  paidBy: List[UUID],
  split: Split
)

sealed trait Split

case object SplitBetweenAll extends Split

case class SplitBetweenMembers(
  userUids: List[UUID]
) extends Split
