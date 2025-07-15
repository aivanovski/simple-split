package com.github.ai.split.api.response

import com.github.ai.split.api.ExpenseDto
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class PostExpenseResponse(
  expense: ExpenseDto
)

object PostExpenseResponse {
  implicit val encoder: JsonEncoder[PostExpenseResponse] = DeriveJsonEncoder.gen[PostExpenseResponse]
  implicit val decoder: JsonDecoder[PostExpenseResponse] = DeriveJsonDecoder.gen[PostExpenseResponse]
}
