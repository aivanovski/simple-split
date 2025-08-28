package com.github.ai.split.api.response

import kotlinx.serialization.Serializable
import com.github.ai.split.api.CurrencyDto

@Serializable
data class GetCurrenciesResponse(
    val currencies: List<CurrencyDto>
)