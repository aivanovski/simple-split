package com.github.ai.split.api

data class TransactionDto(
    val creditorUid: String,
    val debtorUid: String,
    val amount: Double
)