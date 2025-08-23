package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.GroupMemberEntityDao
import com.github.ai.split.api.ExpenseDto
import com.github.ai.split.data.db.repository.{CurrencyRepository, ExpenseRepository}
import com.github.ai.split.entity.db.ExpenseUid
import com.github.ai.split.utils.toExpenseDto
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AssembleExpenseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val currencyRepository: CurrencyRepository,
  private val groupMemberDao: GroupMemberEntityDao,
  private val getAllUsersUseCase: GetAllUsersUseCase
) {

  def assembleExpenseDto(
    expenseUid: ExpenseUid
  ): IO[DomainError, ExpenseDto] = {
    for {
      expense <- expenseRepository.getByUid(expenseUid)
      members <- groupMemberDao.getByGroupUid(groupUid = expense.entity.groupUid)
      userUidToUserMap <- getAllUsersUseCase.getUserUidToUserMap()
      currency <- currencyRepository.getByGroupUid(groupUid = expense.entity.groupUid)
      dto <- toExpenseDto(
        expense = expense,
        currency = currency,
        members = members,
        userUidToUserMap = userUidToUserMap
      )
    } yield dto
  }
}
