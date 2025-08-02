package com.github.ai.split.api

import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class ErrorMessageDto(
  message: Option[String],
  stacktrace: String
)

object ErrorMessageDto {
  implicit val encoder: JsonEncoder[ErrorMessageDto] = DeriveJsonEncoder.gen[ErrorMessageDto]
}
