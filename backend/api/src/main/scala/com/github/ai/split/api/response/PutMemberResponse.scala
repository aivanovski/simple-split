package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class PutMemberResponse(
  group: GroupDto
)

object PutMemberResponse {
  implicit val encoder: JsonEncoder[PutMemberResponse] = DeriveJsonEncoder.gen[PutMemberResponse]
  implicit val decoder: JsonDecoder[PutMemberResponse] = DeriveJsonDecoder.gen[PutMemberResponse]
}
