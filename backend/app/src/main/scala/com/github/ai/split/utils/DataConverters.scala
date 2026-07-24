package com.github.ai.split.utils

import com.github.ai.split.entity.{ExpenseWithRelations, Transaction}
import com.github.ai.split.api.{CurrencyDto, ExpenseDto, GroupDto, MemberDto, TimestampDto, TransactionDto}
import com.github.ai.split.entity.db.{
  CurrencyEntity,
  ExpenseEntity,
  ExpenseUid,
  GroupEntity,
  GroupMembershipEntity,
  MembershipUid,
  PaidByEntity,
  SplitBetweenEntity,
  Timestamp,
  MemberEntity,
  MemberUid
}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

def toExpenseDto(
  expense: ExpenseWithRelations,
  currency: CurrencyEntity,
  members: List[GroupMembershipEntity],
  userUidToUserMap: Map[MemberUid, MemberEntity]
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
  members: List[GroupMembershipEntity],
  paidBy: List[PaidByEntity],
  splitBetween: List[SplitBetweenEntity],
  userUidToUserMap: Map[MemberUid, MemberEntity]
): IO[DomainError, ExpenseDto] = {
  val memberUidToUserUidMap = members.map(member => (member.uid, member.memberUid)).toMap

  for {
    paidByUsers <- toMemberDtos(
      memberUids = paidBy.map(_.membershipUid),
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
          memberUids = splitBetween.map(_.membershipUid),
          memberUidToUserUidMap = memberUidToUserUidMap,
          userUidToUserMap = userUidToUserMap
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
  memberUids: List[MembershipUid],
  memberUidToUserUidMap: Map[MembershipUid, MemberUid],
  userUidToUserMap: Map[MemberUid, MemberEntity]
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
            memberUid.value.toString,
            user.name
          )
        )
        .mapError(_ => DomainError(message = "User not found".some))
    }
  )
}

def toGroupDto(
  group: GroupEntity,
  currency: CurrencyEntity,
  members: List[GroupMembershipEntity],
  expenses: List[ExpenseWithRelations],
  userUidToUserMap: Map[MemberUid, MemberEntity],
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
  members: List[GroupMembershipEntity],
  expenses: List[ExpenseEntity],
  expenseUidToPaidByMap: Map[ExpenseUid, List[PaidByEntity]],
  expenseUidToSplitBetweenMap: Map[ExpenseUid, List[SplitBetweenEntity]],
  userUidToUserMap: Map[MemberUid, MemberEntity],
  paybackTransactions: List[Transaction]
): IO[DomainError, GroupDto] = {
  val memberUidToUserUidMap = members.map(member => (member.uid, member.memberUid)).toMap

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
