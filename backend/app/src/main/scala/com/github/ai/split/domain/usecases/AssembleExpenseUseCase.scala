package com.github.ai.split.domain.usecases

import com.github.ai.split.api.ExpenseDto
import com.github.ai.split.data.db.model.ExpenseUid
import com.github.ai.split.data.db.repository.{CurrencyRepository, ExpenseRepository, GroupRepository}
import com.github.ai.split.utils.toExpenseDto
import com.github.ai.split.entity.exception.DomainError
import zio.*

class AssembleExpenseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val currencyRepository: CurrencyRepository,
  private val groupRepository: GroupRepository
) {

  def assembleExpenseDto(
    expenseUid: ExpenseUid
  ): IO[DomainError, ExpenseDto] = {
    for {
      expense <- expenseRepository.getByUid(expenseUid)
      group <- groupRepository.getByUid(expense.entity.groupUid)
      currency <- currencyRepository.getByGroupUid(groupUid = expense.entity.groupUid)
      dto <- toExpenseDto(
        expense = expense,
        currency = currency,
        members = group.members
      )
    } yield dto
  }
}
