package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao}
import com.github.ai.split.api.GroupDto
import com.github.ai.split.entity.db.GroupUid
import com.github.ai.split.data.db.repository.{CurrencyRepository, ExpenseRepository, GroupRepository}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import com.github.ai.split.utils.*

import java.util.UUID

class AssembleGroupResponseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val currencyRepository: CurrencyRepository,
  private val groupRepository: GroupRepository,
  private val getAllUsersUseCase: GetAllUsersUseCase,
  private val convertExpensesUseCase: ConvertExpensesToTransactionsUseCase,
  private val calculateSettlementUseCase: CalculateSettlementUseCase
) {

  def assembleGroupDto(
    groupUid: GroupUid
  ): IO[DomainError, GroupDto] = {
    for {
      userUidToUserMap <- getAllUsersUseCase.getUserUidToUserMap()
      group <- groupRepository.getByUid(groupUid)
      expenses <- expenseRepository.getByGroupUid(groupUid)
      dto <- {
        val members = group.members.map(_.entity)

        val transactions = convertExpensesUseCase.convertToTransactions(
          expenses = expenses,
          members = members.map(member => member.uid)
        )

        toGroupDto(
          group = group.entity,
          currency = group.currency,
          members = members,
          expenses = expenses,
          userUidToUserMap = userUidToUserMap,
          paybackTransactions = calculateSettlementUseCase.calculateSettlement(transactions)
        )
      }
    } yield dto
  }
}
