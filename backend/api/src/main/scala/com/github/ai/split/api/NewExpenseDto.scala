package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class NewExpenseDto(
  title: String,
  description: Option[String],
  amount: Double,
  paidBy: List[UserNameDto],
  isSplitBetweenAll: Option[Boolean],
  splitBetween: Option[List[UserNameDto]]
)

object NewExpenseDto {
  implicit val encoder: JsonEncoder[NewExpenseDto] = DeriveJsonEncoder.gen[NewExpenseDto]
  implicit val decoder: JsonDecoder[NewExpenseDto] = DeriveJsonDecoder.gen[NewExpenseDto]
}
