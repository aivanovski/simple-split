package com.github.ai.split.entity

case class NewExpense(
  title: String,
  description: String,
  amount: Double,
  paidBy: List[UserReference],
  split: Split
)

sealed trait Split

case object SplitBetweenAll extends Split

case class SplitBetweenMembers(
  members: List[UserReference]
) extends Split
