package com.github.ai.split.api.response

import com.github.ai.split.api.UserDto
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class PostUserResponse(
  user: UserDto
)

object PostUserResponse {
  implicit val encoder: JsonEncoder[PostUserResponse] = DeriveJsonEncoder.gen[PostUserResponse]
  implicit val decoder: JsonDecoder[PostUserResponse] = DeriveJsonDecoder.gen[PostUserResponse]
}
