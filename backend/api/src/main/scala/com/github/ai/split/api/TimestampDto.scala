package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class TimestampDto(
  timestampSeconds: Long,
  formatted: String
)

object TimestampDto {
  implicit val encoder: JsonEncoder[TimestampDto] = DeriveJsonEncoder.gen[TimestampDto]
  implicit val decoder: JsonDecoder[TimestampDto] = DeriveJsonDecoder.gen[TimestampDto]
}
