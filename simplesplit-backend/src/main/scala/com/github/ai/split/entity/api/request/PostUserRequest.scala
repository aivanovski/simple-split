package com.github.ai.split.entity.api.request

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PostUserRequest(
  name: String,
  password: String
)

object PostUserRequest {
  implicit val decoder: JsonDecoder[PostUserRequest] = DeriveJsonDecoder.gen[PostUserRequest]
}
