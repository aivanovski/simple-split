package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupMemberEntityDao}
import com.github.ai.split.api.ExpenseDto
import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.utils.toExpenseDto
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AssembleExpenseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val groupMemberDao: GroupMemberEntityDao,
  private val getAllUsersUseCase: GetAllUsersUseCase
) {

  def assembleExpenseDto(
    expenseUid: UUID
  ): IO[DomainError, ExpenseDto] = {
    for {
      expense <- expenseRepository.getByUid(expenseUid)
      members <- groupMemberDao.getByGroupUid(groupUid = expense.entity.groupUid)
      userUidToUserMap <- getAllUsersUseCase.getUserUidToUserMap()
      dto <- toExpenseDto(
        expense = expense,
        members = members,
        userUidToUserMap = userUidToUserMap
      )
    } yield dto
  }
}
