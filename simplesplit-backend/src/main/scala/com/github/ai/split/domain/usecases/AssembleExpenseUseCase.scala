package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao}
import com.github.ai.split.entity.api.ExpenseDto
import com.github.ai.split.utils.toExpenseDto
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AssembleExpenseUseCase(
  private val expenseDao: ExpenseEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val getAllUsersUseCase: GetAllUsersUseCase
) {

  def assembleExpenseDto(
    expenseUid: UUID
  ): IO[DomainError, ExpenseDto] = {
    for {
      expense <- expenseDao.getByUid(expenseUid)
      members <- groupMemberDao.getByGroupUid(groupUid = expense.groupUid)
      paidBy <- paidByDao.getByExpenseUid(expenseUid)
      splitBetween <- splitBetweenDao.getByExpenseUid(expenseUid)
      userUidToUserMap <- getAllUsersUseCase.getUserUidToUserMap()
      dto <- toExpenseDto(
        expense = expense,
        members = members,
        paidBy = paidBy,
        splitBetween = splitBetween,
        userUidToUserMap = userUidToUserMap
      )
    } yield dto
  }
}
