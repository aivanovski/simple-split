package com.github.ai.split.api.request

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class PutMemberRequest(
  name: String
)

object PutMemberRequest {
  implicit val encoder: JsonEncoder[PutMemberRequest] = DeriveJsonEncoder.gen[PutMemberRequest]
  implicit val decoder: JsonDecoder[PutMemberRequest] = DeriveJsonDecoder.gen[PutMemberRequest]
}
