package com.github.ai.split.api.request

import kotlinx.serialization.Serializable
import com.github.ai.split.api.UserUidDto

@Serializable
data class PostExpenseRequest(
    val title: String,
    val description: String?,
    val amount: Double,
    val paidBy: List<UserUidDto>,
    val isSplitBetweenAll: Boolean?,
    val splitBetween: List<UserUidDto>?
)