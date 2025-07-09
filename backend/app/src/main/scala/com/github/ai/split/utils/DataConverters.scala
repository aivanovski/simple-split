package com.github.ai.split.utils

import com.github.ai.split.entity.Transaction
import com.github.ai.split.api.{ExpenseDto, GroupDto, TransactionDto, UserDto}
import com.github.ai.split.entity.db.{ExpenseEntity, GroupEntity, GroupMemberEntity, PaidByEntity, SplitBetweenEntity, UserEntity}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

def toUserDto(user: UserEntity) = UserDto(
  uid = user.uid.toString,
  name = user.name
)

def toUserDtos(users: List[UserEntity]) = users.map { user => toUserDto(user) }

def toExpenseDto(
  expense: ExpenseEntity,
  members: List[GroupMemberEntity],
  paidBy: List[PaidByEntity],
  splitBetween: List[SplitBetweenEntity],
  userUidToUserMap: Map[UUID, UserEntity]
): IO[DomainError, ExpenseDto] = {
  for {
    paidByUsers <- toUserDtos(paidBy.map(_.userUid), userUidToUserMap)
    splitBetweenUsers <- {
      if (expense.isSplitBetweenAll) {
        toUserDtos(members.map(_.userUid), userUidToUserMap)
      } else {
        toUserDtos(splitBetween.map(_.userUid), userUidToUserMap)
      }
    }
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
            name = user.name
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
  for {
    memberDtos <- toUserDtos(members.map(_.userUid), userUidToUserMap)
    transformedExpenses <- ZIO.collectAll(
      expenses.map(expense =>
        val paidBy = expenseUidToPaidByMap.getOrElse(expense.uid, List.empty)
        val splitBetween = expenseUidToSplitBetweenMap.getOrElse(expense.uid, List.empty)

        toExpenseDto(
          expense = expense,
          members = members,
          paidBy = paidBy,
          splitBetween = splitBetween,
          userUidToUserMap = userUidToUserMap
        )
      )
    )
  } yield GroupDto(
    uid = group.uid.toString,
    title = group.title,
    description = group.description,
    members = memberDtos,
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