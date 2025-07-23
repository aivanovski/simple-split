package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, UserEntityDao}
import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.entity.{ExpenseWithRelations, NewExpense, SplitBetweenAll, SplitBetweenMembers}
import com.github.ai.split.entity.db.{ExpenseEntity, PaidByEntity, SplitBetweenEntity, UserEntity}
import com.github.ai.split.utils.*
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AddExpenseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao
) {

  def addExpenseToGroup(
    groupUid: UUID,
    newExpense: NewExpense
  ): IO[DomainError, ExpenseEntity] = {
    for {
      group <- groupDao.getByUid(uid = groupUid)
      members <- userDao.getByGroupUid(groupUid = groupUid)
      expenses <- expenseRepository.getEntitiesByGroupUid(groupUid = groupUid)

      _ <- isValidExpense(
        expenses = expenses,
        members = members,
        data = newExpense
      )

      expense <- {
        val expenseUid = UUID.randomUUID()

        val splitBetween = newExpense.split match {
          case SplitBetweenAll => List.empty
          case SplitBetweenMembers(splitUids) =>
            splitUids.map(splitUid =>
              SplitBetweenEntity(
                groupUid = groupUid,
                expenseUid = expenseUid,
                userUid = splitUid
              )
            )
        }

        val paidBy = newExpense.paidBy.map(payerUid =>
          PaidByEntity(
            groupUid = groupUid,
            expenseUid = expenseUid,
            userUid = payerUid
          )
        )

        expenseRepository.add(
          ExpenseWithRelations(
            entity = ExpenseEntity(
              uid = expenseUid,
              groupUid = groupUid,
              title = newExpense.title,
              description = newExpense.description,
              amount = newExpense.amount,
              isSplitBetweenAll = newExpense.split == SplitBetweenAll
            ),
            paidBy = paidBy,
            splitBetween = splitBetween
          )
        )
      }
    } yield expense.entity
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