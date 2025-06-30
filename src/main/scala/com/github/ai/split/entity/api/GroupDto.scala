package com.github.ai.split.entity.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GroupDto(
  uid: String,
  owner: UserDto,
  title: String,
  description: String,
  members: List[UserDto],
  expenses: List[ExpenseDto],
  paybackTransactions: List[TransactionDto]
)

object GroupDto {
  implicit val encoder: JsonEncoder[GroupDto] = DeriveJsonEncoder.gen[GroupDto]
  implicit val decoder: JsonDecoder[GroupDto] = DeriveJsonDecoder.gen[GroupDto]
}