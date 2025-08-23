package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ExpenseDto(
  uid: String,
  title: String,
  description: Option[String],
  amount: Double,
  currency: CurrencyDto,
  paidBy: List[MemberDto],
  splitBetween: List[MemberDto]
)

object ExpenseDto {
  implicit val encoder: JsonEncoder[ExpenseDto] = DeriveJsonEncoder.gen[ExpenseDto]
  implicit val decoder: JsonDecoder[ExpenseDto] = DeriveJsonDecoder.gen[ExpenseDto]
}
