package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.entity.{ExpenseWithRelations, Split, SplitBetweenAll, SplitBetweenMembers}
import com.github.ai.split.entity.db.{ExpenseEntity, PaidByEntity, SplitBetweenEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.*

import java.util.UUID

class UpdateExpenseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val userDao: UserEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao
) {

  def updateExpense(
    expenseUid: UUID,
    newTitle: Option[String],
    newDescription: Option[String],
    newAmount: Option[Double],
    newPaidBy: Option[List[UUID]],
    newSplit: Option[Split]
  ): IO[DomainError, UUID] = {
    for {
      expense <- expenseRepository.getByUid(uid = expenseUid)

      _ <- isTitleValid(groupUid = expense.entity.groupUid, expenseUid = expenseUid, title = newTitle)
      _ <- isAmountValid(amount = newAmount)
      _ <- isPaidByValid(groupUid = expense.entity.groupUid, paidBy = newPaidBy)
      _ <- isSplitValid(groupUid = expense.entity.groupUid, split = newSplit)

      // TODO: update expense and other
      _ <- {
        val isSplitBetweenAll = if (newSplit.isDefined) {
          newSplit.get == SplitBetweenAll
        } else {
          expense.entity.isSplitBetweenAll
        }

        val paidBy = if (newPaidBy.isDefined) {
          newPaidBy.get.map { payerUid =>
            PaidByEntity(
              groupUid = expense.entity.groupUid,
              expenseUid = expense.entity.uid,
              userUid = payerUid
            )
          }
        } else {
          expense.paidBy
        }

        val splitBetween = if (newSplit.isDefined) {
          newSplit.get match {
            case SplitBetweenAll => List.empty
            case SplitBetweenMembers(userUids) => userUids.map { userUid =>
              SplitBetweenEntity(
                groupUid = expense.entity.groupUid,
                expenseUid = expense.entity.uid,
                userUid = userUid
              )
            }
          }
        } else {
          expense.splitBetween
        }

        val newExpense = ExpenseWithRelations(
          entity = ExpenseEntity(
            uid = expense.entity.uid,
            groupUid = expense.entity.groupUid,
            title = newTitle.getOrElse(expense.entity.title),
            description = newDescription.getOrElse(expense.entity.description),
            amount = newAmount.getOrElse(expense.entity.amount),
            isSplitBetweenAll = isSplitBetweenAll
          ),
          paidBy = paidBy,
          splitBetween = splitBetween
        )

        expenseRepository.update(newExpense)
      }
    } yield expenseUid
  }

  private def isTitleValid(
    groupUid: UUID,
    expenseUid: UUID,
    title: Option[String]
  ): IO[DomainError, Unit] = {
    if (title.isEmpty) {
      return ZIO.succeed(())
    }

    val newTitle = title.get

    for {
      expenses <- expenseRepository.getEntitiesByGroupUid(groupUid = groupUid)
      otherExpenses = expenses.filter(_.uid != expenseUid)
      _ <- {
        val titleExists = otherExpenses.exists(_.title == newTitle)
        if (titleExists) {
          ZIO.fail(DomainError(message = s"Expense with title '$newTitle' already exists in this group".some))
        } else {
          ZIO.succeed(())
        }
      }
    } yield ()
  }

  private def isAmountValid(
    amount: Option[Double]
  ): IO[DomainError, Unit] = {
    if (amount.isEmpty) {
      return ZIO.succeed(())
    }

    if (amount.get <= 0.0) {
      return ZIO.fail(DomainError(message = s"Invalid amount: ${amount.get}".some))
    }

    ZIO.succeed(())
  }

  private def isPaidByValid(
    groupUid: UUID,
    paidBy: Option[List[UUID]]
  ): IO[DomainError, Unit] = {
    if (paidBy.isEmpty) {
      return ZIO.succeed(())
    }

    val paidByUids = paidBy.getOrElse(List.empty)
    if (paidByUids.size > 1 || paidByUids.isEmpty) {
      return ZIO.fail(DomainError(message = "Wrong number of payers".some))
    }

    for {
      memberUids <- groupMemberDao.getByGroupUid(groupUid = groupUid).map(_.map(_.userUid).toSet)

      _ <- {
        val containsAllPayers = paidByUids.forall(uid => memberUids.contains(uid))
        if (containsAllPayers) {
          ZIO.succeed(())
        } else {
          ZIO.fail(DomainError(message = "Payer should be a member of group".some))
        }
      }
    } yield ()
  }

  private def isSplitValid(
    groupUid: UUID,
    split: Option[Split]
  ): IO[DomainError, Unit] = {
    if (split.isEmpty) {
      return ZIO.succeed(())
    }

    split.get match {
      case SplitBetweenAll => ZIO.succeed(())
      case SplitBetweenMembers(splitUids) => {
        for {
          memberUids <- groupMemberDao.getByGroupUid(groupUid = groupUid).map(_.map(_.userUid).toSet)

          _ <- {
            val containsAllSplittees = splitUids.forall(uid => memberUids.contains(uid))
            if (containsAllSplittees) {
              ZIO.succeed(())
            } else {
              ZIO.fail(DomainError(message = "Invalid split member".some))
            }
          }
        } yield ()
      }
    }
  }
} 