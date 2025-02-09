package com.github.ai.split.domain.usecases

import com.github.ai.split.utils.*
import com.github.ai.split.data.GroupRepository
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.entity.{Expense, Group}
import zio.*

class AddExpenseUseCase(
  private val groupRepository: GroupRepository
) {

  def addExpenseToGroup(expense: Expense, group: Group): IO[DomainError, Group] = {
    val existing = group.expenses.find { e =>
      e.amount == expense.amount && e.title == expense.title
    }
    if (existing.isDefined) {
      return ZIO.fail(DomainError(message = "Expense already exists".some))
    }

    val newGroup = group.copy(expenses = group.expenses :+ expense)

    groupRepository.updateGroup(newGroup)
  }
}
