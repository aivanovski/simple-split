package com.github.ai.split.api.response

import com.github.ai.split.api.ExpenseDto
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class PutExpenseResponse(
  expense: ExpenseDto
)

object PutExpenseResponse {
  implicit val encoder: JsonEncoder[PutExpenseResponse] = DeriveJsonEncoder.gen[PutExpenseResponse]
}
