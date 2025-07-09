package com.github.ai.split.api.response

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class LoginResponse(
  token: String,
  userUid: String
)

object LoginResponse {
  implicit val encoder: JsonEncoder[LoginResponse] = DeriveJsonEncoder.gen[LoginResponse]
  implicit val decoder: JsonDecoder[LoginResponse] = DeriveJsonDecoder.gen[LoginResponse]
}