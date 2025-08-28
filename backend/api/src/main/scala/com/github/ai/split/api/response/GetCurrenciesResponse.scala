package com.github.ai.split.api.response

import com.github.ai.split.api.CurrencyDto
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class GetCurrenciesResponse(
  currencies: List[CurrencyDto]
)

object GetCurrenciesResponse {
  implicit val encoder: JsonEncoder[GetCurrenciesResponse] = DeriveJsonEncoder.gen[GetCurrenciesResponse]
}
