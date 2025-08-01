package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.data.db.repository.{ExpenseRepository, GroupRepository}
import com.github.ai.split.entity.{ExpenseWithRelations, Split, SplitBetweenAll, SplitBetweenMembers, UserReference, Member}
import com.github.ai.split.entity.db.{ExpenseEntity, ExpenseUid, GroupUid, MemberUid, PaidByEntity, SplitBetweenEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import com.github.ai.split.domain.usecases.ResolveUserReferencesUseCase
import zio.*

class UpdateExpenseUseCase(
  private val groupRepository: GroupRepository,
  private val expenseRepository: ExpenseRepository,
  private val userDao: UserEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val resolveUserReferencesUseCase: ResolveUserReferencesUseCase
) {

  def updateExpense(
    expenseUid: ExpenseUid,
    newTitle: Option[String],
    newDescription: Option[String],
    newAmount: Option[Double],
    newPaidBy: Option[List[UserReference]],
    newSplit: Option[Split]
  ): IO[DomainError, ExpenseUid] = {
    for {
      expense <- expenseRepository.getByUid(uid = expenseUid)

      groupUid = expense.entity.groupUid

      members <- groupRepository.getMembers(groupUid)
      _ <- isTitleValid(groupUid = groupUid, expenseUid = expenseUid, title = newTitle)
      _ <- isAmountValid(amount = newAmount)
      _ <- isPaidByValid(groupUid = groupUid, members = members, paidBy = newPaidBy)
      _ <- isSplitValid(groupUid = groupUid, members = members, split = newSplit)

      paidBy <- if (newPaidBy.isDefined) {
        resolveUserReferencesUseCase.resolveReferences(
          allMembers = members,
          references = newPaidBy.getOrElse(List.empty)
        ).map { payers =>
          payers.map { payer =>
            PaidByEntity(
              groupUid = groupUid,
              expenseUid = expense.entity.uid,
              memberUid = payer.entity.uid
            )
          }
        }
      } else {
        ZIO.succeed(expense.paidBy)
      }

      splitBetween <- if (newSplit.isDefined) {
        val split = newSplit.get match {
          case SplitBetweenAll => List.empty
          case SplitBetweenMembers(references) => resolveUserReferencesUseCase.resolveReferences(
            allMembers = members,
            references = references
          ).map { splitMembers =>
            splitMembers.map { member =>
              SplitBetweenEntity(
                groupUid = groupUid,
                expenseUid = expense.entity.uid,
                memberUid = member.entity.uid
              )
            }
          }
        }

        ZIO.succeed(List.empty)
      } else {
        ZIO.succeed(expense.splitBetween)
      }

      _ <- {
        val isSplitBetweenAll = if (newSplit.isDefined) {
          newSplit.get == SplitBetweenAll
        } else {
          expense.entity.isSplitBetweenAll
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
    groupUid: GroupUid,
    expenseUid: ExpenseUid,
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
    groupUid: GroupUid,
    members: List[Member],
    paidBy: Option[List[UserReference]]
  ): IO[DomainError, Unit] = {
    if (paidBy.isEmpty) {
      return ZIO.succeed(())
    }

    val paidByRefs = paidBy.getOrElse(List.empty)
    if (paidByRefs.size > 1 || paidByRefs.isEmpty) {
      return ZIO.fail(DomainError(message = "Wrong number of payers".some))
    }

    resolveUserReferencesUseCase.validateReferences(
      allMembers = members,
      references = paidByRefs
    )
  }

  private def isSplitValid(
    groupUid: GroupUid,
    members: List[Member],
    split: Option[Split]
  ): IO[DomainError, Unit] = {
    if (split.isEmpty) {
      return ZIO.succeed(())
    }

    split.get match {
      case SplitBetweenAll => ZIO.succeed(())
      case SplitBetweenMembers(references) => resolveUserReferencesUseCase.validateReferences(
        allMembers = members,
        references = references
      )
    }
  }
} 