package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class PostGroupResponse(
  group: GroupDto
)

object PostGroupResponse {
  implicit val encoder: JsonEncoder[PostGroupResponse] = DeriveJsonEncoder.gen[PostGroupResponse]
}
