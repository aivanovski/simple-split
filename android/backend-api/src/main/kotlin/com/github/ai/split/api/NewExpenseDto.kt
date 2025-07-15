package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class NewExpenseDto(
    val title: String,
    val description: String?,
    val amount: Double,
    val paidBy: List<UserNameDto>,
    val isSplitBetweenAll: Boolean?,
    val splitBetween: List<UserNameDto>?
)