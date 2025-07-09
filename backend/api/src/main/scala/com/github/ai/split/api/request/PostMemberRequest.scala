package com.github.ai.split.api.request

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PostMemberRequest(
  uid: String
)

object PostMemberRequest {
  implicit val decoder: JsonDecoder[PostMemberRequest] = DeriveJsonDecoder.gen[PostMemberRequest]
}
