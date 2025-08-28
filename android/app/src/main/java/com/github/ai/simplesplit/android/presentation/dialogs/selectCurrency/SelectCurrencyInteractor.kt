package com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency

import arrow.core.Either
import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.simplesplit.android.data.repository.CurrencyRepository
import com.github.ai.simplesplit.android.model.exception.AppException

class SelectCurrencyInteractor(
    private val currencyRepository: CurrencyRepository
) {

    fun getCurrencies(): Either<AppException, List<CurrencyEntity>> =
        currencyRepository.getAllCached()
            .map { currencies ->
                currencies.sortedBy { currency ->
                    currency.name
                }
            }
}