package com.github.ai.split.domain.usecases

import com.github.ai.split.api.GroupDto
import com.github.ai.split.data.db.model.GroupUid
import com.github.ai.split.data.db.repository.{CurrencyRepository, ExpenseRepository, GroupRepository}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import com.github.ai.split.utils.*

class AssembleGroupResponseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val currencyRepository: CurrencyRepository,
  private val groupRepository: GroupRepository,
  private val convertExpensesUseCase: ConvertExpensesToTransactionsUseCase,
  private val calculateSettlementUseCase: CalculateSettlementUseCase
) {

  def assembleGroupDto(
    groupUid: GroupUid
  ): IO[DomainError, GroupDto] = {
    for {
      group <- groupRepository.getByUid(groupUid)
      expenses <- expenseRepository.getByGroupUid(groupUid)
      dto <- {
        val members = group.members

        val transactions = convertExpensesUseCase.convertToTransactions(
          expenses = expenses,
          members = members.map(_.member.uid)
        )

        toGroupDto(
          group = group.entity,
          currency = group.currency,
          members = members,
          expenses = expenses,
          paybackTransactions = calculateSettlementUseCase.calculateSettlement(transactions)
        )
      }
    } yield dto
  }
}
