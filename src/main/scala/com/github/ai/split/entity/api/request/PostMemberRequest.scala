package com.github.ai.split.entity.api.request

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PostMemberRequest(
  email: String
)

object PostMemberRequest {
  implicit val decoder: JsonDecoder[PostMemberRequest] = DeriveJsonDecoder.gen[PostMemberRequest]
}
