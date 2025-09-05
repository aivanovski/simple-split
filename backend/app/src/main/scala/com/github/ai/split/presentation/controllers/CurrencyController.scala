package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.CurrencyDto
import com.github.ai.split.api.response.GetCurrenciesResponse
import com.github.ai.split.data.JsonSerializer
import com.github.ai.split.entity.db.CurrencyEntity
import com.github.ai.split.data.db.repository.CurrencyRepository
import com.github.ai.split.utils.toJavaList
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*
import zio.http.Response

class CurrencyController(
  private val currencyRepository: CurrencyRepository,
  private val jsonSerializer: JsonSerializer
) {
  def getCurrencies(): IO[DomainError, Response] = {
    defer {
      val currencies = currencyRepository.getAll().run

      Response.json(jsonSerializer.serialize(createResponse(currencies)))
    }
  }

  private def createResponse(currencies: List[CurrencyEntity]): GetCurrenciesResponse = {
    new GetCurrenciesResponse(
      currencies
        .map { currency =>
          new CurrencyDto(
            currency.isoCode,
            currency.name,
            currency.symbol
          )
        }
        .toJavaList()
    )
  }
}
