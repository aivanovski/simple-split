package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    val uid: String,
    val title: String,
    val description: String?,
    val amount: Double,
    val currency: CurrencyDto,
    val paidBy: List<MemberDto>,
    val splitBetween: List<MemberDto>
)