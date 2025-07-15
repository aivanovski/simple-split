package com.github.ai.split.api.response

import kotlinx.serialization.Serializable
import com.github.ai.split.api.ExpenseDto

@Serializable
data class PostExpenseResponse(
    val expense: ExpenseDto
)