package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.entity.NewExpense
import com.github.ai.split.entity.db.{ExpenseEntity, GroupEntity, PaidByEntity, SplitBetweenEntity, UserEntity}
import com.github.ai.split.utils.*
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AddExpenseUseCase(
  private val accessResolver: AccessResolverService,
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
  private val expenseDao: ExpenseEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao
) {

  def addExpenseToGroup(
    user: UserEntity,
    groupUid: UUID,
    data: NewExpense
  ): IO[DomainError, ExpenseEntity] = {
    for {
      _ <- accessResolver.canModifyGroup(userUid = user.uid, groupUid = groupUid)
      group <- groupDao.getByUid(groupUid)
      owner <- userDao.getByUid(group.ownerUid)

      // TODO: create use case for getting members
      members <- groupMemberDao.getByGroupUid(groupUid)
        .flatMap(members =>
          ZIO.collectAll(
            members.map(member => userDao.getByUid(member.userUid))
          )
        )

      expenses <- expenseDao.getByGroupUid(groupUid)
      
      _ <- isValidExpense(
        expenses = expenses,
        owner = owner,
        members = members,
        data = data
      )

      expense <- expenseDao.add(
        ExpenseEntity(
          uid = UUID.randomUUID(),
          groupUid = groupUid,
          title = data.title,
          description = data.description,
          amount = data.amount
        )
      )

      payers <- paidByDao.add(
        data.paidBy.map(payerUid =>
          PaidByEntity(
            groupUid = groupUid,
            expenseUid = expense.uid,
            userUid = payerUid
          )
        )
      )
      
      splits <- splitBetweenDao.add(
        data.splitBetween.map(splitUid =>
          SplitBetweenEntity(
            groupUid = groupUid,
            expenseUid = expense.uid,
            userUid = splitUid
          )
        )
      )

    } yield expense
  }

  private def isValidExpense(
    expenses: List[ExpenseEntity],
    owner: UserEntity,
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

    val isSpliteeInMembers = data.splitBetween.forall(split => memberUids.contains(split))
    if (!isSpliteeInMembers) {
      return ZIO.fail(DomainError(message = "Invalid splitting".some))
    }

    ZIO.succeed(())
  }
}