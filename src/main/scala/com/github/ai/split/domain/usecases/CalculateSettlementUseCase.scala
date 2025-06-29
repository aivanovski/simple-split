package com.github.ai.split.domain.usecases

import com.github.ai.split.entity.{Expense, Transaction}

import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class CalculateSettlementUseCase {

  def calculateSettlement(
    transactions: List[Transaction]
  ): List[Transaction] = {
    val balances = mutable.Map[UUID, Double]()

    for (transaction <- transactions) {
      val creditorBalance = balances.getOrElse(transaction.creditor, 0.0)
      val debtorBalance = balances.getOrElse(transaction.debtor, 0.0)

      balances.put(transaction.creditor, creditorBalance + transaction.amount)
      balances.put(transaction.debtor, debtorBalance - transaction.amount)
    }

    val creditors = ListBuffer[(UUID, Double)]()
    val debtors = ListBuffer[(UUID, Double)]()

    for ((person, balance) <- balances) {
      if (balance > 0) {
        creditors.addOne((person, balance))
      } else if (balance < 0) {
        debtors.addOne((person, -balance)) // Store as positive for easier calculation
      }
    }

    val simplifiedTransactions = ListBuffer[Transaction]()

    // Use while loops with peek() and poll() for LinkedList to simulate pointers and efficient removal
    while (debtors.nonEmpty && creditors.nonEmpty) {
      var (debtorUid, debtorAmount) = debtors.head
      var (creditorUid, creditorAmount) = creditors.head

      // Determine the amount that can be settled in this transaction
      val settleAmount = Math.min(debtorAmount, creditorAmount)

      if (settleAmount > 0) {
        simplifiedTransactions.addOne(
          Transaction(
            creditor = creditorUid,
            debtor = debtorUid,
            amount = settleAmount
          )
        )
      }

      // Update remaining amounts
      debtorAmount -= settleAmount
      creditorAmount -= settleAmount

      // Update the head of the lists or remove if settled
      if (debtorAmount == 0.0) {
        debtors.remove(0) // Remove fully settled debtor
      } else {
        debtors.remove(0) // Remove and re-add with updated amount
        debtors.prepend((debtorUid, debtorAmount))
      }

      if (creditorAmount == 0.0) {
        creditors.remove(0) // Remove fully settled creditor
      } else {
        creditors.remove(0) // Remove and re-add with updated amount
        creditors.prepend((creditorUid, creditorAmount))
      }
    }

    simplifiedTransactions.toList
  }
}
