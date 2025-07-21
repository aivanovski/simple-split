package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GetGroupsResponse(
  groups: List[GroupDto]
)

object GetGroupsResponse {
  implicit val encoder: JsonEncoder[GetGroupsResponse] = DeriveJsonEncoder.gen[GetGroupsResponse]
}