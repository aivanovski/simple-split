package com.github.ai.split.domain.usecases

import com.github.ai.split.data.currency.CurrencyParser
import com.github.ai.split.data.db.repository.CurrencyRepository
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class FillCurrencyDataUseCase(
  private val repository: CurrencyRepository,
  private val currencyParser: CurrencyParser
) {

  def parseAndFillCurrencyData(): IO[DomainError, Unit] = {
    defer {
      val currencies = currencyParser
        .parse()
        .run
        .distinctBy(currency => currency.isoCode)

      val existingCurrencies = repository
        .getAll()
        .run
        .map(currency => (currency.isoCode, currency))
        .toMap

      for (newCurrency <- currencies) {
        val existingCurrency = existingCurrencies.get(newCurrency.isoCode)

        if (existingCurrency.isDefined) {
          val existing = existingCurrency.get
          if (existing.name != newCurrency.name || existing.symbol != newCurrency.symbol) {
            repository.update(newCurrency).run
          }
        } else {
          repository.add(newCurrency).run
        }
      }
    }
  }
}
