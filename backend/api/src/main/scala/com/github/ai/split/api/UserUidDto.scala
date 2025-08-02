package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class UserUidDto(
  uid: String
)

object UserUidDto {
  implicit val encoder: JsonEncoder[UserUidDto] = DeriveJsonEncoder.gen[UserUidDto]
  implicit val decoder: JsonDecoder[UserUidDto] = DeriveJsonDecoder.gen[UserUidDto]
}
