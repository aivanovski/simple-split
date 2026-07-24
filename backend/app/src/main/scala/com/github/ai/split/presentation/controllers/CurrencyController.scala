package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.CurrencyDto
import com.github.ai.split.api.response.GetCurrenciesResponse
import com.github.ai.split.data.db.model.CurrencyEntity
import com.github.ai.split.data.db.repository.CurrencyRepository
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class CurrencyController(
  private val currencyRepository: CurrencyRepository
) {
  def getCurrencies(): IO[DomainError, GetCurrenciesResponse] = {
    defer {
      val currencies = currencyRepository.getAll().run
      createResponse(currencies)
    }
  }

  private def createResponse(currencies: List[CurrencyEntity]): GetCurrenciesResponse = {
    new GetCurrenciesResponse(
      currencies.map { currency =>
        new CurrencyDto(
          currency.isoCode,
          currency.name,
          currency.symbol
        )
      }.toList
    )
  }
}
