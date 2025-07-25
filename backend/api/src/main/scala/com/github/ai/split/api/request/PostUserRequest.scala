package com.github.ai.split.api.request

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PostUserRequest(
  name: String
)

object PostUserRequest {
  implicit val decoder: JsonDecoder[PostUserRequest] = DeriveJsonDecoder.gen[PostUserRequest]
}
