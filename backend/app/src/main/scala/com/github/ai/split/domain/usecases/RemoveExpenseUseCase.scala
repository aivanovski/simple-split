package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.entity.db.ExpenseUid
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class RemoveExpenseUseCase(
  private val expenseRepository: ExpenseRepository
) {

  def remvoveExpense(
    expenseUid: ExpenseUid
  ): IO[DomainError, Unit] =
    expenseRepository.removeByUid(expenseUid)
}
