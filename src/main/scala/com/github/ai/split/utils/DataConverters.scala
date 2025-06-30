package com.github.ai.split.utils

import com.github.ai.split.entity.Transaction
import com.github.ai.split.entity.api.{ExpenseDto, GroupDto, TransactionDto, UserDto}
import com.github.ai.split.entity.db.{ExpenseEntity, GroupEntity, GroupMemberEntity, PaidByEntity, SplitBetweenEntity, UserEntity}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

def toUserDto(user: UserEntity) = UserDto(
  uid = user.uid.toString,
  email = user.email
)

def toUserDtos(users: List[UserEntity]) = users.map { user => toUserDto(user) }

def toExpenseDto(
  expense: ExpenseEntity,
  paidBy: List[PaidByEntity],
  splitBetween: List[SplitBetweenEntity],
  userUidToUserMap: Map[UUID, UserEntity]
): IO[DomainError, ExpenseDto] = {
  for {
    paidByUsers <- toUserDtos(paidBy.map(_.userUid), userUidToUserMap)
    splitBetweenUsers <- toUserDtos(splitBetween.map(_.userUid), userUidToUserMap)
  } yield ExpenseDto(
    uid = expense.uid.toString,
    title = expense.title,
    description = expense.description.some,
    amount = expense.amount,
    paidBy = paidByUsers,
    splitBetween = splitBetweenUsers
  )
}

def toUserDtos(
  uids: List[UUID],
  userUidToUserMap: Map[UUID, UserEntity]
): IO[DomainError, List[UserDto]] = {
  ZIO.collectAll(
    uids.map { uid =>
      ZIO.fromOption(userUidToUserMap.get(uid))
        .map(user =>
          UserDto(
            uid = user.uid.toString,
            email = user.email
          )
        )
        .mapError(_ => DomainError(message = "User not found".some))
    }
  )
}

def toGroupDto(
  group: GroupEntity,
  members: List[GroupMemberEntity],
  expenses: List[ExpenseEntity],
  expenseUidToPaidByMap: Map[UUID, List[PaidByEntity]],
  expenseUidToSplitBetweenMap: Map[UUID, List[SplitBetweenEntity]],
  userUidToUserMap: Map[UUID, UserEntity],
  paybackTransactions: List[Transaction]
): IO[DomainError, GroupDto] = {
  val owner = userUidToUserMap.get(group.ownerUid)
  if (owner.isEmpty) {
    return ZIO.fail(DomainError(message = "User not found".some))
  }

  for {
    members <- toUserDtos(members.map(_.userUid), userUidToUserMap)
    transformedExpenses <- ZIO.collectAll(
      expenses.map(expense =>
        val paidBy = expenseUidToPaidByMap.getOrElse(expense.uid, List.empty)
        val splitBetween = expenseUidToSplitBetweenMap.getOrElse(expense.uid, List.empty)

        toExpenseDto(
          expense = expense,
          paidBy = paidBy,
          splitBetween = splitBetween,
          userUidToUserMap = userUidToUserMap
        )
      )
    )
  } yield GroupDto(
    uid = group.uid.toString,
    owner = toUserDto(owner.get),
    title = group.title,
    description = group.description,
    members = members,
    expenses = transformedExpenses,
    paybackTransactions = paybackTransactions.map(transaction => toTransactionDto(transaction))
  )
}

def toTransactionDto(
  transaction: Transaction
): TransactionDto =
  TransactionDto(
    creditorUid = transaction.creditor.toString,
    debtorUid = transaction.debtor.toString,
    amount = transaction.amount
  )