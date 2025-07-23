package com.github.ai.split.entity

import com.github.ai.split.entity.db.{ExpenseEntity, PaidByEntity, SplitBetweenEntity}

case class ExpenseWithRelations(
  entity: ExpenseEntity,
  paidBy: List[PaidByEntity],
  splitBetween: List[SplitBetweenEntity]
)
