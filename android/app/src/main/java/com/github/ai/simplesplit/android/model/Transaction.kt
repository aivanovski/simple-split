package com.github.ai.simplesplit.android.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val creditorUid: String,
    val debtorUid: String,
    val amount: Double
)