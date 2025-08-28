package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.CurrencyDto
import com.github.ai.split.api.response.GetCurrenciesResponse
import com.github.ai.split.data.db.repository.CurrencyRepository
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*
import zio.http.Response
import zio.json.*

class CurrencyController(
  private val currencyRepository: CurrencyRepository
) {

  def getCurrencies(): IO[DomainError, Response] = {
    defer {
      val currencies = currencyRepository.getAll().run

      val response = GetCurrenciesResponse(
        currencies.map { currency =>
          CurrencyDto(
            isoCode = currency.isoCode,
            name = currency.name,
            symbol = currency.symbol
          )
        }
      )

      Response.json(response.toJsonPretty)
    }
  }
}
