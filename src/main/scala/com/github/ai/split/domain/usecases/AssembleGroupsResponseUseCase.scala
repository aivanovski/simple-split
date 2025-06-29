package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao}
import com.github.ai.split.entity.api.GroupDto
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toGroupDto
import zio.*

import java.util.UUID

class AssembleGroupsResponseUseCase(
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val expenseDao: ExpenseEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val getAllUsersUseCase: GetAllUsersUseCase,
  private val convertExpensesUseCase: ConvertExpensesToTransactionsUseCase,
  private val settlementCalculator: CalculateSettlementUseCase
) {

  def assembleGroupDtos(
    userUid: UUID
  ): IO[DomainError, List[GroupDto]] = {
    for {
      userUidToUserMap <- getAllUsersUseCase.getUserUidToUserMap()
      // TODO: check access to group
      allGroups <- groupDao.getAll()
      allMembers <- groupMemberDao.getAll()
      allExpenses <- expenseDao.getAll()
      allPaidBy <- paidByDao.getAll()
      allSplitBetween <- splitBetweenDao.getAll()
      data <- {
        val groupUidToMembersMap = allMembers.groupBy(_.groupUid)
        val groupUidToExpenseMap = allExpenses.groupBy(_.groupUid)
        val expenseUidToPaidByMap = allPaidBy.groupBy(_.expenseUid)
        val expenseUidToSplitBetweenMap = allSplitBetween.groupBy(_.expenseUid)

        ZIO.collectAll(
          allGroups
            .map { group =>
              val groupExpenses = groupUidToExpenseMap.getOrElse(group.uid, List.empty)
              val groupPaidBy = allPaidBy.filter(_.groupUid == group.uid)
              val groupSplitBetween = allSplitBetween.filter(_.groupUid == group.uid)

              val groupTransactions = convertExpensesUseCase.convertToTransactions(
                expenses = groupExpenses,
                paidBy = groupPaidBy,
                splitBetween = groupSplitBetween
              )

              val paybackTransactions = settlementCalculator.calculateSettlement(groupTransactions)

              toGroupDto(
                group = group,
                members = groupUidToMembersMap.getOrElse(group.uid, List.empty),
                expenses = groupUidToExpenseMap.getOrElse(group.uid, List.empty),
                expenseUidToPaidByMap = expenseUidToPaidByMap,
                expenseUidToSplitBetweenMap = expenseUidToSplitBetweenMap,
                userUidToUserMap = userUidToUserMap,
                paybackTransactions = paybackTransactions
              )
            }
        )
      }
    } yield data
  }
}
