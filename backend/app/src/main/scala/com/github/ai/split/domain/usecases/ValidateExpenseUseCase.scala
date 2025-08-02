package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.repository.{ExpenseRepository, GroupRepository}
import com.github.ai.split.entity.db.ExpenseEntity
import com.github.ai.split.entity.{
  Member,
  Split,
  SplitBetweenMembers,
  SplitBetweenAll,
  MemberReference,
  NameReference,
  NewExpense,
  NewUser,
  UserReference
}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

class ValidateExpenseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val groupRepository: GroupRepository
) {

  def validateExpenseData(
    members: List[NewUser | Member],
    currentExpenses: List[ExpenseEntity],
    expense: NewExpense
  ): IO[DomainError, Unit] = {
    defer {
      if (members.isEmpty) {
        ZIO.fail(DomainError(message = s"No member in the group".some)).run
      }

      if (expense.amount <= 0.0) {
        ZIO.fail(DomainError(message = s"Invalid payment amount: ${expense.amount}".some)).run
      }

      if (currentExpenses.exists(_.title == expense.title)) {
        ZIO.fail(DomainError(message = s"Expense with the same title already exists: ${expense.title}".some)).run
      }

      validatePaidBy(members = members, paidBy = expense.paidBy).run
      validateSplit(members = members, split = expense.split).run

      ZIO.unit.run
    }
  }

  private def validatePaidBy(
    members: List[NewUser | Member],
    paidBy: List[UserReference]
  ): IO[DomainError, Unit] = {
    for {
      _ <-
        if (paidBy.isEmpty) {
          ZIO.fail(DomainError(message = "Payer is not specified".some))
        } else {
          ZIO.unit
        }

      _ <- ZIO.collectAll(
        paidBy.map { payer =>
          resolveReference(members = members, reference = payer)
        }
      )
    } yield ()
  }

  private def validateSplit(
    members: List[NewUser | Member],
    split: Split
  ): IO[DomainError, Unit] = {
    defer {
      split match {
        case SplitBetweenMembers(splitMembers) => {
          if (splitMembers.isEmpty) {
            ZIO.fail(DomainError(message = "No split members".some)).run
          }

          ZIO
            .collectAll(
              splitMembers.map { memberReference =>
                resolveReference(members = members, reference = memberReference)
              }
            )
            .run
        }

        case _ =>
      }

      ZIO.unit.run
    }
  }

  private def resolveReference(
    members: List[NewUser | Member],
    reference: UserReference
  ): IO[DomainError, Unit] = {
    val memberNames = members.map {
      case newUser: NewUser => newUser.name
      case m: Member => m.user.name
    }

    val memberUids = members
      .map {
        case _: NewUser => None
        case member: Member => Some(member.entity.uid)
      }
      .filter(uid => uid.isDefined)
      .map(uid => uid.get)

    reference match {
      case MemberReference(memberUid) => {
        if (!memberUids.contains(memberUid)) {
          return ZIO.fail(DomainError(message = s"Invalid member uid: $memberUid".some))
        }
      }

      case NameReference(name) => {
        if (!memberNames.contains(name)) {
          return ZIO.fail(DomainError(message = s"Invalid member: $name".some))
        }
      }
    }

    ZIO.unit
  }
}
