package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.entity.api.GroupDto
import com.github.ai.split.entity.exception.DomainError
import zio.*
import com.github.ai.split.utils.*

import java.util.UUID

class AssembleGroupResponseUseCase(
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val expenseDao: ExpenseEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val getAllUsersUseCase: GetAllUsersUseCase,
  private val convertExpensesUseCase: ConvertExpensesToTransactionsUseCase,
  private val calculateSettlementUseCase: CalculateSettlementUseCase
) {

  def assembleGroupDto(
    groupUid: UUID
  ): IO[DomainError, GroupDto] = {
    for {
      userUidToUserMap <- getAllUsersUseCase.getUserUidToUserMap()
      group <- groupDao.getByUid(groupUid)
      members <- groupMemberDao.getByGroupUid(groupUid)
      expenses <- expenseDao.getByGroupUid(groupUid)
      paidBy <- paidByDao.getByGroupUid(groupUid)
      splitBetween <- splitBetweenDao.getByGroupUid(groupUid)
      dto <- {
        val transactions = convertExpensesUseCase.convertToTransactions(
          expenses = expenses,
          paidBy = paidBy,
          splitBetween = splitBetween
        )

        val paybackTransactions = calculateSettlementUseCase.calculateSettlement(transactions)

        toGroupDto(
          group = group,
          members = members,
          expenses = expenses,
          expenseUidToPaidByMap = paidBy.groupBy(_.expenseUid),
          expenseUidToSplitBetweenMap = splitBetween.groupBy(_.expenseUid),
          userUidToUserMap = userUidToUserMap,
          paybackTransactions = paybackTransactions
        )
      }
    } yield dto
  }
}
