package com.github.ai.split.domain.usecases

import com.github.ai.split.entity.Transaction
import com.github.ai.split.entity.db.{ExpenseEntity, PaidByEntity, SplitBetweenEntity}

import scala.collection.mutable.ListBuffer

class ConvertExpensesToTransactionsUseCase {

  def convertToTransactions(
    expenses: List[ExpenseEntity],
    paidBy: List[PaidByEntity],
    splitBetween: List[SplitBetweenEntity]
  ): List[Transaction] = {
    val transactions = ListBuffer[Transaction]()

    val expenseUidToPaidByMap = paidBy.groupBy(_.expenseUid)
    val expenseUidToSplitBetweenMap = splitBetween.groupBy(_.expenseUid)

    for (expense <- expenses) {
      val payments = expenseUidToPaidByMap.getOrElse(expense.uid, List.empty)
      val splits = expenseUidToSplitBetweenMap.getOrElse(expense.uid, List.empty)

      for (payment <- payments) {
        val amount = expense.amount / splits.size
        val creditorUid = payment.userUid

        for (split <- splits.filter(s => s.userUid != creditorUid)) {
          transactions.addOne(
            Transaction(
              creditor = creditorUid,
              debtor = split.userUid,
              amount = amount
            )
          )
        }
      }
    }

    transactions.toList
  }
}
