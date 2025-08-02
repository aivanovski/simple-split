package com.github.ai.split.api

import zio.json._

case class MemberDto(
  uid: String,
  name: String
)

object MemberDto {
  implicit val encoder: JsonEncoder[MemberDto] = DeriveJsonEncoder.gen[MemberDto]
  implicit val decoder: JsonDecoder[MemberDto] = DeriveJsonDecoder.gen[MemberDto]
}
