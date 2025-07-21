package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class PutGroupResponse(
  group: GroupDto
)

object PutGroupResponse {
  implicit val encoder: JsonEncoder[PutGroupResponse] = DeriveJsonEncoder.gen[PutGroupResponse]
}
