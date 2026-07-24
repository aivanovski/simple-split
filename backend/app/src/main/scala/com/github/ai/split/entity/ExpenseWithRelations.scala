package com.github.ai.split.entity

import com.github.ai.split.data.db.model.{ExpenseEntity, PaidByEntity, SplitBetweenEntity}

case class ExpenseWithRelations(
  entity: ExpenseEntity,
  paidBy: List[PaidByEntity],
  splitBetween: List[SplitBetweenEntity]
)
