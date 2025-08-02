package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class PostMemberResponse(
  group: GroupDto
)

object PostMemberResponse {
  implicit val encoder: JsonEncoder[PostMemberResponse] = DeriveJsonEncoder.gen[PostMemberResponse]
}
