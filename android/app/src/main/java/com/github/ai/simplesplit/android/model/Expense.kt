package com.github.ai.simplesplit.android.model

import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val uid: String,
    val title: String,
    val description: String,
    val amount: Double,
    val currency: CurrencyEntity,
    val paidBy: List<Member>,
    val splitBetween: List<Member>,
    val createdSeconds: Long,
    val modifiedSeconds: Long
)