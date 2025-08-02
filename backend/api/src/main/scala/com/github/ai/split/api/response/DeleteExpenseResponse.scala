package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class DeleteExpenseResponse(
  group: GroupDto
)

object DeleteExpenseResponse {
  implicit val encoder: JsonEncoder[DeleteExpenseResponse] = DeriveJsonEncoder.gen[DeleteExpenseResponse]
}
