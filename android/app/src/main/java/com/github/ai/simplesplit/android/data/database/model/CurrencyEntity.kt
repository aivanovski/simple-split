package com.github.ai.simplesplit.android.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("currencies")
data class CurrencyEntity(
    @PrimaryKey
    val isoCode: String,
    val name: String,
    val symbol: String
)