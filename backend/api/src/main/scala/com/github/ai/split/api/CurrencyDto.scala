package com.github.ai.split.api

import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class CurrencyDto(
  isoCode: String,
  name: String,
  symbol: String
)

object CurrencyDto {
  implicit val encoder: JsonEncoder[CurrencyDto] = DeriveJsonEncoder.gen[CurrencyDto] 
}