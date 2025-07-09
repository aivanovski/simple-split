package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class UserUid(
  uid: String
)

object UserUid {
  implicit val decoder: JsonDecoder[UserUid] = DeriveJsonDecoder.gen[UserUid]
}