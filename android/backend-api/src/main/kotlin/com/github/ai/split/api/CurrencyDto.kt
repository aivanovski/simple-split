package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyDto(
    val isoCode: String,
    val name: String,
    val symbol: String
)