package com.github.ai.split.api.request

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class PostMemberRequest(
  groupUid: String,
  name: String
)

object PostMemberRequest {
  implicit val encoder: JsonEncoder[PostMemberRequest] = DeriveJsonEncoder.gen[PostMemberRequest]
  implicit val decoder: JsonDecoder[PostMemberRequest] = DeriveJsonDecoder.gen[PostMemberRequest]
}
