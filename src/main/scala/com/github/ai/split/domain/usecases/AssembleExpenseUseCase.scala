package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, PaidByEntityDao, SplitBetweenEntityDao}
import com.github.ai.split.entity.api.ExpenseDto
import com.github.ai.split.utils.toExpenseDto
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AssembleExpenseUseCase(
  private val expenseDao: ExpenseEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val getAllUsersUseCase: GetAllUsersUseCase
) {

  def assembleExpenseDto(
    expenseUid: UUID
  ): IO[DomainError, ExpenseDto] = {
    for {
      expense <- expenseDao.getByUid(expenseUid)
      paidBy <- paidByDao.getByExpenseUid(expenseUid)
      splitBetween <- splitBetweenDao.getByExpenseUid(expenseUid)
      userUidToUserMap <- getAllUsersUseCase.getUserUidToUserMap()
      dto <- toExpenseDto(
        expense = expense,
        paidBy = paidBy,
        splitBetween = splitBetween,
        userUidToUserMap = userUidToUserMap
      )
    } yield dto
  }
}
