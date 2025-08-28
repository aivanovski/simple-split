package com.github.ai.split.utils

import com.github.ai.split.entity.{ExpenseWithRelations, Transaction}
import com.github.ai.split.api.{CurrencyDto, ExpenseDto, GroupDto, MemberDto, TransactionDto}
import com.github.ai.split.entity.db.{
  CurrencyEntity,
  ExpenseEntity,
  ExpenseUid,
  GroupEntity,
  GroupMemberEntity,
  MemberUid,
  PaidByEntity,
  SplitBetweenEntity,
  UserEntity,
  UserUid
}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

import java.util.UUID

def toExpenseDto(
  expense: ExpenseWithRelations,
  currency: CurrencyEntity,
  members: List[GroupMemberEntity],
  userUidToUserMap: Map[UserUid, UserEntity]
): IO[DomainError, ExpenseDto] =
  toExpenseDto(
    expense = expense.entity,
    currency = currency,
    members = members,
    paidBy = expense.paidBy,
    splitBetween = expense.splitBetween,
    userUidToUserMap = userUidToUserMap
  )

def toExpenseDto(
  expense: ExpenseEntity,
  currency: CurrencyEntity,
  members: List[GroupMemberEntity],
  paidBy: List[PaidByEntity],
  splitBetween: List[SplitBetweenEntity],
  userUidToUserMap: Map[UserUid, UserEntity]
): IO[DomainError, ExpenseDto] = {
  val memberUidToUserUidMap = members.map(member => (member.uid, member.userUid)).toMap

  for {
    paidByUsers <- toMemberDtos(
      memberUids = paidBy.map(_.memberUid),
      memberUidToUserUidMap = memberUidToUserUidMap,
      userUidToUserMap = userUidToUserMap
    )

    splitBetweenUsers <- {
      if (expense.isSplitBetweenAll) {
        toMemberDtos(
          memberUids = members.map(_.uid),
          memberUidToUserUidMap = memberUidToUserUidMap,
          userUidToUserMap = userUidToUserMap
        )
      } else {
        toMemberDtos(
          memberUids = splitBetween.map(_.memberUid),
          memberUidToUserUidMap = memberUidToUserUidMap,
          userUidToUserMap = userUidToUserMap
        )
      }
    }
  } yield ExpenseDto(
    uid = expense.uid.toString,
    title = expense.title,
    description = expense.description.some,
    amount = expense.amount,
    currency = toCurrencyDto(currency),
    paidBy = paidByUsers,
    splitBetween = splitBetweenUsers
  )
}

def toMemberDtos(
  memberUids: List[MemberUid],
  memberUidToUserUidMap: Map[MemberUid, UserUid],
  userUidToUserMap: Map[UserUid, UserEntity]
): IO[DomainError, List[MemberDto]] = {
  ZIO.collectAll(
    memberUids.map { memberUid =>
      val userOption = memberUidToUserUidMap
        .get(memberUid)
        .flatMap(userUid => userUidToUserMap.get(userUid))

      ZIO
        .fromOption(userOption)
        .map(user =>
          MemberDto(
            uid = memberUid.toString,
            name = user.name
          )
        )
        .mapError(_ => DomainError(message = "User not found".some))
    }
  )
}

def toGroupDto(
  group: GroupEntity,
  currency: CurrencyEntity,
  members: List[GroupMemberEntity],
  expenses: List[ExpenseWithRelations],
  userUidToUserMap: Map[UserUid, UserEntity],
  paybackTransactions: List[Transaction]
): IO[DomainError, GroupDto] =
  toGroupDto(
    group = group,
    currency = currency,
    members = members,
    expenses = expenses.map(_.entity),
    expenseUidToPaidByMap = expenses.map(expense => (expense.entity.uid, expense.paidBy)).toMap,
    expenseUidToSplitBetweenMap = expenses.map(expense => (expense.entity.uid, expense.splitBetween)).toMap,
    userUidToUserMap = userUidToUserMap,
    paybackTransactions = paybackTransactions
  )

def toGroupDto(
  group: GroupEntity,
  currency: CurrencyEntity,
  members: List[GroupMemberEntity],
  expenses: List[ExpenseEntity],
  expenseUidToPaidByMap: Map[ExpenseUid, List[PaidByEntity]],
  expenseUidToSplitBetweenMap: Map[ExpenseUid, List[SplitBetweenEntity]],
  userUidToUserMap: Map[UserUid, UserEntity],
  paybackTransactions: List[Transaction]
): IO[DomainError, GroupDto] = {
  val memberUidToUserUidMap = members.map(member => (member.uid, member.userUid)).toMap

  for {
    memberDtos <- toMemberDtos(
      memberUids = members.map(_.uid),
      memberUidToUserUidMap = memberUidToUserUidMap,
      userUidToUserMap = userUidToUserMap
    )

    transformedExpenses <- ZIO.collectAll(
      expenses.map(expense =>
        val paidBy = expenseUidToPaidByMap.getOrElse(expense.uid, List.empty)
        val splitBetween = expenseUidToSplitBetweenMap.getOrElse(expense.uid, List.empty)

        toExpenseDto(
          expense = expense,
          currency = currency,
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
    currency = toCurrencyDto(currency),
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

def toCurrencyDto(
  currency: CurrencyEntity
): CurrencyDto =
  CurrencyDto(
    isoCode = currency.isoCode,
    name = currency.name,
    symbol = currency.symbol
  )
