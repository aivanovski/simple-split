package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val uid: String,
    val title: String,
    val description: String,
    val members: List<UserDto>,
    val expenses: List<ExpenseDto>,
    val paybackTransactions: List<TransactionDto>
)