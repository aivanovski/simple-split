package com.github.ai.split.entity.api.request

import com.github.ai.split.entity.api.UserUid
import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PostExpenseRequest(
  title: String,
  description: Option[String],
  amount: Double,
  paidBy: List[UserUid],
  isSplitBetweenAll: Option[Boolean],
  splitBetween: Option[List[UserUid]]
)

object PostExpenseRequest {
  implicit val decoder: JsonDecoder[PostExpenseRequest] = DeriveJsonDecoder.gen[PostExpenseRequest]
}
