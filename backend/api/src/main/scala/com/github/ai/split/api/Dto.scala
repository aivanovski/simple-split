package com.github.ai.split.api

final case class CurrencyDto(
  isoCode: String = "",
  name: String = "",
  symbol: String = ""
)

final case class TimestampDto(
  timestampSeconds: Long = 0L,
  formatted: String = ""
)

final case class MemberDto(
  uid: String = "",
  name: String = ""
)

final case class UserNameDto(name: String = "")

final case class UserUidDto(uid: String = "")

final case class TransactionDto(
  creditorUid: String = "",
  debtorUid: String = "",
  amount: Double = 0.0
)

final case class ExpenseDto(
  uid: String = "",
  title: String = "",
  description: String = "",
  amount: Double = 0.0,
  currency: CurrencyDto = CurrencyDto(),
  paidBy: List[MemberDto] = Nil,
  splitBetween: List[MemberDto] = Nil,
  created: TimestampDto = TimestampDto(),
  modified: TimestampDto = TimestampDto()
)

final case class NewExpenseDto(
  title: String = "",
  description: String = "",
  amount: Double = 0.0,
  paidBy: List[UserNameDto] = Nil,
  isSplitBetweenAll: Option[Boolean] = None,
  splitBetween: List[UserNameDto] = Nil
)

final case class GroupDto(
  uid: String = "",
  title: String = "",
  description: String = "",
  currency: CurrencyDto = CurrencyDto(),
  members: List[MemberDto] = Nil,
  expenses: List[ExpenseDto] = Nil,
  paybackTransactions: List[TransactionDto] = Nil,
  created: TimestampDto = TimestampDto(),
  modified: TimestampDto = TimestampDto()
)

final case class GetGroupErrorDto(
  uid: String = "",
  message: String = ""
)

final case class ErrorMessageDto(
  message: Option[String] = None,
  exception: String = "",
  stacktraceBase64: String = "",
  stacktraceLines: List[String] = Nil
)
