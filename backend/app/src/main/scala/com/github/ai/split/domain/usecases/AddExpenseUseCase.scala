package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao, UserEntityDao}
import com.github.ai.split.data.db.repository.{ExpenseRepository, GroupRepository}
import com.github.ai.split.entity.{ExpenseWithRelations, Member, MemberReference, NameReference, NewExpense, SplitBetweenAll, SplitBetweenMembers, UserReference}
import com.github.ai.split.entity.db.{ExpenseEntity, ExpenseUid, GroupMemberEntity, GroupUid, MemberUid, PaidByEntity, SplitBetweenEntity, UserEntity}
import com.github.ai.split.utils.*
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.domain.usecases.{ResolveUserReferencesUseCase, ValidateExpenseUseCase}
import zio.*
import zio.direct.*

import java.util.UUID

class AddExpenseUseCase(
  private val expenseRepository: ExpenseRepository,
  private val groupRepository: GroupRepository,
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val resolveUserUseCase: ResolveUserReferencesUseCase,
  private val validateExpenseUseCase: ValidateExpenseUseCase
) {

  def addExpenseToGroup(
    groupUid: GroupUid,
    newExpense: NewExpense
  ): IO[DomainError, ExpenseEntity] = {
    defer {
      val group = groupDao.getByUid(groupUid).run
      val members = groupRepository.getMembers(groupUid).run
      val expenses = expenseRepository.getEntitiesByGroupUid(groupUid).run

      validateExpenseUseCase.validateExpenseData(
        members = members,
        currentExpenses = expenses,
        expense = newExpense
      ).run

      val expenseUid = ExpenseUid(UUID.randomUUID())

      val paidByMembers = resolveUserUseCase.resolveReferences(
        allMembers = members,
        references = newExpense.paidBy
      ).run

      val splitMembers = resolveUserUseCase.resolveReferences(
        allMembers = members,
        references = newExpense.split match {
          case SplitBetweenMembers(references) => references
          case SplitBetweenAll => List.empty
        }
      ).run

      val splitBetween = splitMembers.map { splitMember =>
        SplitBetweenEntity(
          groupUid = groupUid,
          expenseUid = expenseUid,
          memberUid = splitMember.entity.uid
        )
      }

      val paidBy = paidByMembers.map { payer =>
        PaidByEntity(
          groupUid = groupUid,
          expenseUid = expenseUid,
          memberUid = payer.entity.uid
        )
      }

      val expense = expenseRepository.add(
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
      ).run

      expense.entity
    }
  }

  private def validateExpenseData(
    expenses: List[ExpenseEntity],
    paidByMembers: List[Member],
    splitMembers: List[Member],
    data: NewExpense
  ): IO[DomainError, Unit] = {
    defer {
      if (data.amount <= 0.0) {
        ZIO.fail(DomainError(message = s"Invalid payment amount: ${data.amount}".some)).run
      }

      if (expenses.exists(_.title == data.title)) {
        ZIO.fail(DomainError(message = s"Expense with the same title already exists: ${data.title}".some)).run
      }

      if (data.paidBy.isEmpty) {
        ZIO.fail(DomainError(message = "No payer specified".some)).run
      }

      if (paidByMembers.isEmpty) {
        ZIO.fail(DomainError(message = "Payer is not specified".some)).run
      }

      if (splitMembers.nonEmpty && data.split == SplitBetweenAll) {
        ZIO.fail(DomainError(message = "Invalid split".some)).run
      }
    }
  }
}