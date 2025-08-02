package com.github.ai.split.api.request

import com.github.ai.split.api.UserUidDto
import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PutExpenseRequest(
  title: Option[String],
  description: Option[String],
  amount: Option[Double],
  paidBy: Option[List[UserUidDto]],
  isSplitBetweenAll: Option[Boolean],
  splitBetween: Option[List[UserUidDto]]
)

object PutExpenseRequest {
  implicit val decoder: JsonDecoder[PutExpenseRequest] = DeriveJsonDecoder.gen[PutExpenseRequest]
}
