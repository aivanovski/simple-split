package com.github.ai.split.api

final case class CurrencyDto(
  isoCode: String,
  name: String,
  symbol: String
)

final case class TimestampDto(
  timestampSeconds: Long,
  formatted: String
)

final case class MemberDto(
  uid: String,
  name: String
)

final case class UserNameDto(name: String)

final case class UserUidDto(uid: String)

final case class TransactionDto(
  creditorUid: String,
  debtorUid: String,
  amount: Double
)

final case class ExpenseDto(
  uid: String,
  title: String,
  description: String,
  amount: Double,
  currency: CurrencyDto,
  paidBy: List[MemberDto],
  splitBetween: List[MemberDto],
  created: TimestampDto,
  modified: TimestampDto
)

final case class NewExpenseDto(
  title: String,
  description: String,
  amount: Double,
  paidBy: List[UserNameDto],
  isSplitBetweenAll: Option[Boolean],
  splitBetween: List[UserNameDto]
)

final case class GroupDto(
  uid: String,
  title: String,
  description: String,
  currency: CurrencyDto,
  members: List[MemberDto],
  expenses: List[ExpenseDto],
  paybackTransactions: List[TransactionDto],
  created: TimestampDto,
  modified: TimestampDto
)

final case class GetGroupErrorDto(
  uid: String,
  message: String
)

final case class ErrorMessageDto(
  message: Option[String],
  exception: String,
  stacktraceBase64: String,
  stacktraceLines: List[String]
)
