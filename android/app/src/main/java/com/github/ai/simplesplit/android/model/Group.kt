package com.github.ai.simplesplit.android.model

import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val uid: String,
    val title: String,
    val description: String,
    val currency: CurrencyEntity,
    val members: List<Member>,
    val expenses: List<Expense>,
    val paybackTransactions: List<Transaction>,
    val createdSeconds: Long,
    val modifiedSeconds: Long
)