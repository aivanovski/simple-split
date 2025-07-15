package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class UserNameDto(
  name: String
)

object UserNameDto {
  implicit val encoder: JsonEncoder[UserNameDto] = DeriveJsonEncoder.gen[UserNameDto]
  implicit val decoder: JsonDecoder[UserNameDto] = DeriveJsonDecoder.gen[UserNameDto]
}
