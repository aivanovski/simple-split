package com.github.ai.split.api.response

import com.github.ai.split.api.{GetGroupErrorDto, GroupDto}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GetGroupsResponse(
  groups: List[GroupDto],
  errors: List[GetGroupErrorDto]
)

object GetGroupsResponse {
  implicit val encoder: JsonEncoder[GetGroupsResponse] = DeriveJsonEncoder.gen[GetGroupsResponse]
  implicit val decoder: JsonDecoder[GetGroupsResponse] = DeriveJsonDecoder.gen[GetGroupsResponse]
}
