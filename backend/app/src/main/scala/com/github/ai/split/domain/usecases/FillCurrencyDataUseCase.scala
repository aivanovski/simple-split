package com.github.ai.split.domain.usecases

import com.github.ai.split.data.currency.CurrencyParser
import com.github.ai.split.data.db.dao.CurrencyEntityDao
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class FillCurrencyDataUseCase(
  private val currencyDao: CurrencyEntityDao,
  private val currencyParser: CurrencyParser
) {

  def parseAndFillCurrencyData(): IO[DomainError, Unit] = {
    defer {
      val currencies = CurrencyParser().parse().run

      val savedCurrencies = currencyDao.getAll()
        .run
        .map(currency => (currency.isoCode, currency))
        .toMap

      for (newCurrency <- currencies) {
        val savedCurrency = savedCurrencies.get(newCurrency)
        
      }
      
      ZIO.logInfo(s"Parsed currencies: ${currencies.size}").run
    }
  }
}
