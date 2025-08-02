package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GroupDto(
  uid: String,
  title: String,
  description: String,
  members: List[MemberDto],
  expenses: List[ExpenseDto],
  paybackTransactions: List[TransactionDto]
)

object GroupDto {
  implicit val encoder: JsonEncoder[GroupDto] = DeriveJsonEncoder.gen[GroupDto]
  implicit val decoder: JsonDecoder[GroupDto] = DeriveJsonDecoder.gen[GroupDto]
}
