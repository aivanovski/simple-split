package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class GetGroupErrorDto(
  uid: String,
  message: String
)

object GetGroupErrorDto {
  implicit val encoder: JsonEncoder[GetGroupErrorDto] = DeriveJsonEncoder.gen[GetGroupErrorDto]
  implicit val decoder: JsonDecoder[GetGroupErrorDto] = DeriveJsonDecoder.gen[GetGroupErrorDto]
}
