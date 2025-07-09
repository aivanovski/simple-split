package com.github.ai.split.api

import zio.json._

case class UserDto(
  uid: String,
  name: String
)

object UserDto {
  implicit val encoder: JsonEncoder[UserDto] = DeriveJsonEncoder.gen[UserDto]
  implicit val decoder: JsonDecoder[UserDto] = DeriveJsonDecoder.gen[UserDto]
}