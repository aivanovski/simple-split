package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class UserUidDto(
  uid: String
)

object UserUidDto {
  implicit val decoder: JsonDecoder[UserUidDto] = DeriveJsonDecoder.gen[UserUidDto]
}