package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class DeleteMemberResponse(
  group: GroupDto
)

object DeleteMemberResponse {
  implicit val encoder: JsonEncoder[DeleteMemberResponse] = DeriveJsonEncoder.gen[DeleteMemberResponse]
}
