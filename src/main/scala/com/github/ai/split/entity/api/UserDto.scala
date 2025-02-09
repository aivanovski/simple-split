package com.github.ai.split.entity.api

import zio.schema.DeriveSchema._
import zio.json._

case class UserDto(uid: String, email: String)

object UserDto {
  implicit val encoder: JsonEncoder[UserDto] = DeriveJsonEncoder.gen[UserDto]
  implicit val decoder: JsonDecoder[UserDto] = DeriveJsonDecoder.gen[UserDto]
}