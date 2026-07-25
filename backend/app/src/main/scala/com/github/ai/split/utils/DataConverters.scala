package com.github.ai.split.utils

import com.github.ai.split.entity.{ExpenseWithRelations, MemberWithUser, Transaction}
import com.github.ai.split.api.{CurrencyDto, ExpenseDto, GroupDto, MemberDto, TimestampDto, TransactionDto}
import com.github.ai.split.data.db.model.{
  CurrencyEntity,
  ExpenseEntity,
  ExpenseUid,
  GroupEntity,
  MemberUid,
  PaidByEntity,
  SplitBetweenEntity,
  Timestamp
}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

def toExpenseDto(
  expense: ExpenseWithRelations,
  currency: CurrencyEntity,
  members: List[MemberWithUser]
): IO[DomainError, ExpenseDto] =
  toExpenseDto(
    expense = expense.entity,
    currency = currency,
    members = members,
    paidBy = expense.paidBy,
    splitBetween = expense.splitBetween
  )

def toExpenseDto(
  expense: ExpenseEntity,
  currency: CurrencyEntity,
  members: List[MemberWithUser],
  paidBy: List[PaidByEntity],
  splitBetween: List[SplitBetweenEntity]
): IO[DomainError, ExpenseDto] = {
  for {
    paidByUsers <- toMemberDtos(
      memberUids = paidBy.map(_.memberUid),
      members = members
    )

    splitBetweenUsers <- {
      if (expense.isSplitBetweenAll) {
        toMemberDtos(
          memberUids = members.map(_.member.uid),
          members = members
        )
      } else {
        toMemberDtos(
          memberUids = splitBetween.map(_.memberUid),
          members = members
        )
      }
    }
  } yield ExpenseDto(
    expense.uid.value.toString,
    expense.title,
    expense.description,
    expense.amount,
    toCurrencyDto(currency),
    paidByUsers,
    splitBetweenUsers,
    toTimestampDto(expense.created),
    toTimestampDto(expense.modified)
  )
}

def toMemberDtos(
  memberUids: List[MemberUid],
  members: List[MemberWithUser]
): IO[DomainError, List[MemberDto]] = {
  ZIO.collectAll(
    memberUids.map { memberUid =>
      val userOption = members.find(_.member.uid == memberUid)

      ZIO
        .fromOption(userOption)
        .map(member =>
          MemberDto(
            memberUid.value.toString,
            member.getName()
          )
        )
        .mapError(_ => DomainError(message = "User not found".some))
    }
  )
}

def toGroupDto(
  group: GroupEntity,
  currency: CurrencyEntity,
  members: List[MemberWithUser],
  expenses: List[ExpenseWithRelations],
  paybackTransactions: List[Transaction]
): IO[DomainError, GroupDto] =
  toGroupDto(
    group = group,
    currency = currency,
    members = members,
    expenses = expenses.map(_.entity),
    expenseUidToPaidByMap = expenses.map(expense => (expense.entity.uid, expense.paidBy)).toMap,
    expenseUidToSplitBetweenMap = expenses.map(expense => (expense.entity.uid, expense.splitBetween)).toMap,
    paybackTransactions = paybackTransactions
  )

def toGroupDto(
  group: GroupEntity,
  currency: CurrencyEntity,
  members: List[MemberWithUser],
  expenses: List[ExpenseEntity],
  expenseUidToPaidByMap: Map[ExpenseUid, List[PaidByEntity]],
  expenseUidToSplitBetweenMap: Map[ExpenseUid, List[SplitBetweenEntity]],
  paybackTransactions: List[Transaction]
): IO[DomainError, GroupDto] = {
  for {
    memberDtos <- toMemberDtos(
      memberUids = members.map(_.member.uid),
      members = members
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
          splitBetween = splitBetween
        )
      )
    )
  } yield GroupDto(
    group.uid.value.toString,
    group.title,
    group.description,
    toCurrencyDto(currency),
    memberDtos,
    transformedExpenses,
    paybackTransactions.map(transaction => toTransactionDto(transaction)),
    toTimestampDto(group.created),
    toTimestampDto(group.modified)
  )
}

def toTransactionDto(
  transaction: Transaction
): TransactionDto =
  TransactionDto(
    transaction.creditor.value.toString,
    transaction.debtor.value.toString,
    transaction.amount
  )

def toCurrencyDto(
  currency: CurrencyEntity
): CurrencyDto =
  CurrencyDto(
    currency.isoCode,
    currency.name,
    currency.symbol
  )

def toTimestampDto(
  timestamp: Timestamp
): TimestampDto = {
  val time = LocalDateTime.ofEpochSecond(timestamp.seconds, 0, ZoneOffset.UTC)

  TimestampDto(
    timestamp.seconds,
    time.format(TIMESTAMP_FORMAT)
  )
}

private val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
