package com.github.ai.simplesplit.android.data.api.coverters

import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.split.api.CurrencyDto

fun List<CurrencyDto>.toCurrencies(): List<CurrencyEntity> {
    return this.map { dto -> dto.toCurrency() }
}

fun CurrencyDto.toCurrency(): CurrencyEntity =
    CurrencyEntity(
        isoCode = isoCode,
        name = name,
        symbol = symbol
    )