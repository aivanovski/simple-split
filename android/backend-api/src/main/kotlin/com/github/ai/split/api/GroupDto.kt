package com.github.ai.split.api

data class GroupDto(
    val uid: String,
    val title: String,
    val description: String,
    val members: List<UserDto>,
    val expenses: List<ExpenseDto>,
    val paybackTransactions: List<TransactionDto>
)