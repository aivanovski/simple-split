package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao}
import com.github.ai.split.api.GroupDto
import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.entity.exception.DomainError
import zio.*
import com.github.ai.split.utils.*

import java.util.UUID

class AssembleGroupResponseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
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
      expenses <- expenseRepository.getByGroupUid(groupUid)
      dto <- {
        val transactions = convertExpensesUseCase.convertToTransactions(
          expenses = expenses,
          members = members.map(member => member.userUid)
        )

        toGroupDto(
          group = group,
          members = members,
          expenses = expenses,
          userUidToUserMap = userUidToUserMap,
          paybackTransactions = calculateSettlementUseCase.calculateSettlement(transactions)
        )
      }
    } yield dto
  }
}
