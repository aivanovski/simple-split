package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val creditorUid: String,
    val debtorUid: String,
    val amount: Double
)