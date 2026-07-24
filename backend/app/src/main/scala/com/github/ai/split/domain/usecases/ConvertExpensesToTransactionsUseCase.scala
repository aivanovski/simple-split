package com.github.ai.split.domain.usecases

import com.github.ai.split.entity.{ExpenseWithRelations, Transaction}
import com.github.ai.split.entity.db.{ExpenseEntity, MembershipUid, PaidByEntity, SplitBetweenEntity}

import java.util.UUID
import scala.collection.mutable.ListBuffer

class ConvertExpensesToTransactionsUseCase {

  def convertToTransactions(
    expenses: List[ExpenseWithRelations],
    members: List[MembershipUid]
  ): List[Transaction] =
    convertToTransactions(
      expenses = expenses.map(_.entity),
      members = members,
      paidBy = expenses.flatMap(_.paidBy),
      splitBetween = expenses.flatMap(_.splitBetween)
    )

  def convertToTransactions(
    expenses: List[ExpenseEntity],
    members: List[MembershipUid],
    paidBy: List[PaidByEntity],
    splitBetween: List[SplitBetweenEntity]
  ): List[Transaction] = {
    val transactions = ListBuffer[Transaction]()

    val expenseUidToPaidByMap = paidBy.groupBy(_.expenseUid)
    val expenseUidToSplitBetweenMap = splitBetween.groupBy(_.expenseUid)

    for (expense <- expenses) {
      val payments = expenseUidToPaidByMap.getOrElse(expense.uid, List.empty)
      val splitUserUids = if (expense.isSplitBetweenAll) {
        members
      } else {
        expenseUidToSplitBetweenMap
          .getOrElse(expense.uid, List.empty)
          .map(split => split.membershipUid)
      }

      for (payment <- payments) {
        val amount = if (expense.isSplitBetweenAll) {
          expense.amount / members.size
        } else {
          expense.amount / splitUserUids.size
        }

        val creditorUid = payment.membershipUid

        for (splitUserUid <- splitUserUids.filter(uid => uid != creditorUid)) {
          transactions.addOne(
            Transaction(
              creditor = creditorUid,
              debtor = splitUserUid,
              amount = amount
            )
          )
        }
      }
    }

    transactions.toList
  }
}
