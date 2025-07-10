package com.github.ai.split.api.request

import com.github.ai.split.api.UserUid

data class PostExpenseRequest(
    val title: String,
    val description: String?,
    val amount: Double,
    val paidBy: List<UserUid>,
    val isSplitBetweenAll: Boolean?,
    val splitBetween: List<UserUid>?
)