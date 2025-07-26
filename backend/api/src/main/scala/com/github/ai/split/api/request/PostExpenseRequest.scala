package com.github.ai.split.api.request

import com.github.ai.split.api.UserUidDto
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class PostExpenseRequest(
  groupUid: String,
  title: String,
  description: Option[String],
  amount: Double,
  paidBy: List[UserUidDto],
  isSplitBetweenAll: Option[Boolean],
  splitBetween: Option[List[UserUidDto]]
)

object PostExpenseRequest {
  implicit val encoder: JsonEncoder[PostExpenseRequest] = DeriveJsonEncoder.gen[PostExpenseRequest]
  implicit val decoder: JsonDecoder[PostExpenseRequest] = DeriveJsonDecoder.gen[PostExpenseRequest]
}
