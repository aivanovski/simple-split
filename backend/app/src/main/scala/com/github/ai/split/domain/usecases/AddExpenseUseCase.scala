package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.domain.{AccessResolverService, PasswordService}
import com.github.ai.split.entity.{NewExpense, Split, SplitBetweenAll, SplitBetweenMembers, UidReference, UserReference}
import com.github.ai.split.entity.db.{ExpenseEntity, GroupEntity, PaidByEntity, SplitBetweenEntity, UserEntity}
import com.github.ai.split.utils.*
import com.github.ai.split.entity.exception.DomainError
import com.sun.tools.javac.code.Type.ForAll
import zio.*

import java.util.UUID

class AddExpenseUseCase(
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
  private val expenseDao: ExpenseEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao
) {

  def addExpenseToGroup(
    groupUid: UUID,
    newExpense: NewExpense
  ): IO[DomainError, ExpenseEntity] = {
    for {
      group <- groupDao.getByUid(groupUid)
      members <- userDao.getByGroupUid(groupUid = groupUid)
      expenses <- expenseDao.getByGroupUid(groupUid)

      _ <- isValidExpense(
        expenses = expenses,
        members = members,
        data = newExpense
      )

      expense <- expenseDao.add(
        ExpenseEntity(
          uid = UUID.randomUUID(),
          groupUid = groupUid,
          title = newExpense.title,
          description = newExpense.description,
          amount = newExpense.amount,
          isSplitBetweenAll = newExpense.split == SplitBetweenAll
        )
      )

      payers <- paidByDao.add(
        newExpense.paidBy.map(payerUid =>
          PaidByEntity(
            groupUid = groupUid,
            expenseUid = expense.uid,
            userUid = payerUid
          )
        )
      )

      _ <- {
        newExpense.split match
          case SplitBetweenAll => ZIO.succeed(())
          case SplitBetweenMembers(splitUids) =>
            splitBetweenDao.add(
              splitUids.map(splitUid =>
                SplitBetweenEntity(
                  groupUid = groupUid,
                  expenseUid = expense.uid,
                  userUid = splitUid
                )
              )
            )
      }

    } yield expense
  }

  private def isValidExpense(
    expenses: List[ExpenseEntity],
    members: List[UserEntity],
    data: NewExpense
  ): IO[DomainError, Unit] = {
    if (data.amount <= 0.0) {
      return ZIO.fail(DomainError(message = s"Invalid payment amount: ${data.amount}".some))
    }

    if (expenses.exists(_.title == data.title)) {
      return ZIO.fail(DomainError(message = s"Expense with the same title already exists: ${data.title}".some))
    }

    if (data.paidBy.isEmpty) {
      return ZIO.fail(DomainError(message = "No payer specified".some))
    }

    val memberUids = members.map(_.uid).toSet
    val isPayersInMembers = data.paidBy.forall(payer => memberUids.contains(payer))
    if (!isPayersInMembers) {
      return ZIO.fail(DomainError(message = "Payer is not a member of the group".some))
    }

    data.split match {
      case SplitBetweenMembers(userUids) => {
        val isSpliteeInMembers = userUids.forall(split => memberUids.contains(split))
        if (!isSpliteeInMembers) {
          return ZIO.fail(DomainError(message = "Invalid splitting".some))
        }
      }
      case _ => 
    }

    ZIO.succeed(())
  }
}