package com.github.ai.simplesplit.android.utils

import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity

fun Double.formatAsMoney(currency: CurrencyEntity?): String {
    val formatted = "%.2f".format(this)

    val whole = formatted.substringBefore('.')
    val fraction = formatted.substringAfter('.')
    val fractionValue = fraction.toIntOrNull()

    val symbol = if (!currency?.symbol.isNullOrBlank()) {
        currency?.symbol
    } else {
        currency?.isoCode ?: StringUtils.EMPTY
    }

    return if (fractionValue == 0) {
        "$symbol$whole"
    } else {
        "$symbol$formatted"
    }
}