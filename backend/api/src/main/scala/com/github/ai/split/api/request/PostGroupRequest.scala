package com.github.ai.split.api.request

import com.github.ai.split.api.{NewExpenseDto, UserDto, UserNameDto, UserUid}
import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PostGroupRequest(
  password: String,
  title: String,
  description: Option[String],
  members: Option[List[UserNameDto]],
  expenses: Option[List[NewExpenseDto]]
)

object PostGroupRequest {
  implicit val decoder: JsonDecoder[PostGroupRequest] = DeriveJsonDecoder.gen[PostGroupRequest]
}
