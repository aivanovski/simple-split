package com.github.ai.split.entity.api.response

import com.github.ai.split.entity.api.GroupDto
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GetGroupsResponse(
  groups: List[GroupDto]
)

object GetGroupsResponse {
  implicit val encoder: JsonEncoder[GetGroupsResponse] = DeriveJsonEncoder.gen[GetGroupsResponse]
  implicit val decoder: JsonDecoder[GetGroupsResponse] = DeriveJsonDecoder.gen[GetGroupsResponse]
}