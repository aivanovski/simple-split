package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, PaidByEntityDao, SplitBetweenEntityDao}
import com.github.ai.split.domain.usecases.GetAllUsersUseCase
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class CalculateSettlementUseCase(
  private val expenseDao: ExpenseEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val getAllUsersUseCase: GetAllUsersUseCase
) {

  def calculateDebtSettlement(
    groupUid: UUID
  ): IO[DomainError, Unit] = {
    for {
      expenses <- expenseDao.getByGroupUid(groupUid)
      paidBy <- paidByDao.getByGroupUid(groupUid)
      splitBetween <- splitBetweenDao.getByGroupUid(groupUid)
      userUidToUserMap <- getAllUsersUseCase.getUserUidToUserMap()

      // A: 90 -> A B C
      // B: 180 -> A B C
      // C: 360 -> A B C
      //// 630/3 = 210
      //
      // A: (30 - 90) + 60 + 120 => 120
      // B: 30 + (60 - 180) + 120
      // C: 30 + 60 + (120 - 360)

    } yield ()
  }
}
