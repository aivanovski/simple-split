package com.github.ai.split.api.response

import com.github.ai.split.api.ExpenseDto
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class PostExpenseResponse(
  expense: ExpenseDto
)

object PostExpenseResponse {
  implicit val encoder: JsonEncoder[PostExpenseResponse] = DeriveJsonEncoder.gen[PostExpenseResponse]
}
