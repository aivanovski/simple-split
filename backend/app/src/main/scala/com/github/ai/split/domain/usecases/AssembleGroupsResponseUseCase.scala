package com.github.ai.split.domain.usecases

import com.github.ai.split.api.GroupDto
import com.github.ai.split.data.db.dao.{PaidByEntityDao, SplitBetweenEntityDao}
import com.github.ai.split.data.db.model.GroupUid
import com.github.ai.split.data.db.repository.{ExpenseRepository, GroupRepository}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toGroupDto
import zio.*

class AssembleGroupsResponseUseCase(
  private val groupRepository: GroupRepository,
  private val expenseRepository: ExpenseRepository,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val convertExpensesUseCase: ConvertExpensesToTransactionsUseCase,
  private val settlementCalculator: CalculateSettlementUseCase
) {

  def assembleGroupDtos(
    uids: List[GroupUid]
  ): IO[DomainError, List[GroupDto]] = {
    for {
      // TODO: Optimize DB querying
      groups <- groupRepository.getByUids(uids)
      allExpenses <- expenseRepository.getEntitiesByGroupUids(uids)
      allPaidBy <- paidByDao.getAll()
      allSplitBetween <- splitBetweenDao.getAll()

      data <- {
        val groupUidToExpenseMap = allExpenses.groupBy(_.groupUid)
        val expenseUidToPaidByMap = allPaidBy.groupBy(_.expenseUid)
        val expenseUidToSplitBetweenMap = allSplitBetween.groupBy(_.expenseUid)

        ZIO.collectAll(
          groups
            .map { group =>
              val groupExpenses = groupUidToExpenseMap.getOrElse(group.entity.uid, List.empty)
              val groupPaidBy = allPaidBy.filter(_.groupUid == group.entity.uid)
              val groupSplitBetween = allSplitBetween.filter(_.groupUid == group.entity.uid)

              val groupTransactions = convertExpensesUseCase.convertToTransactions(
                expenses = groupExpenses,
                members = group.members.map(_.member.uid),
                paidBy = groupPaidBy,
                splitBetween = groupSplitBetween
              )

              val paybackTransactions = settlementCalculator.calculateSettlement(groupTransactions)

              toGroupDto(
                group = group.entity,
                currency = group.currency,
                members = group.members,
                expenses = groupUidToExpenseMap.getOrElse(group.entity.uid, List.empty),
                expenseUidToPaidByMap = expenseUidToPaidByMap,
                expenseUidToSplitBetweenMap = expenseUidToSplitBetweenMap,
                paybackTransactions = paybackTransactions
              )
            }
        )
      }
    } yield data
  }
}
