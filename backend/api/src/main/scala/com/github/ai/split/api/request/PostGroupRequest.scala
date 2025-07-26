package com.github.ai.split.api.request

import com.github.ai.split.api.{NewExpenseDto, UserNameDto}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class PostGroupRequest(
  password: String,
  title: String,
  description: Option[String],
  members: Option[List[UserNameDto]],
  expenses: Option[List[NewExpenseDto]]
)

object PostGroupRequest {
  implicit val encoder: JsonEncoder[PostGroupRequest] = DeriveJsonEncoder.gen[PostGroupRequest]
  implicit val decoder: JsonDecoder[PostGroupRequest] = DeriveJsonDecoder.gen[PostGroupRequest]
}
