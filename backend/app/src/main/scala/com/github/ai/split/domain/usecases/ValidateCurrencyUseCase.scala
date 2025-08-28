package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.repository.CurrencyRepository
import com.github.ai.split.entity.exception.DomainError
import zio.*

class ValidateCurrencyUseCase(
  private val currencyRepository: CurrencyRepository
) {

  def isCurrencyIsoCodeValid(isoCode: String): IO[DomainError, Unit] =
    currencyRepository
      .getByIsoCode(isoCode)
      .map(_ => ())
}
