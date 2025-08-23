package com.github.ai.split.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class CurrencyDto(
  isoCode: String,
  name: String,
  symbol: String
)

object CurrencyDto {
  implicit val encoder: JsonEncoder[CurrencyDto] = DeriveJsonEncoder.gen[CurrencyDto]
  implicit val decoder: JsonDecoder[CurrencyDto] = DeriveJsonDecoder.gen[CurrencyDto]
}
