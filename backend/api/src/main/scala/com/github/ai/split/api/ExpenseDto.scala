package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ExpenseDto(
  uid: String,
  title: String,
  description: Option[String],
  amount: Double,
  paidBy: List[UserDto],
  splitBetween: List[UserDto]
)

object ExpenseDto {
  implicit val encoder: JsonEncoder[ExpenseDto] = DeriveJsonEncoder.gen[ExpenseDto]
  implicit val decoder: JsonDecoder[ExpenseDto] = DeriveJsonDecoder.gen[ExpenseDto]
}