package com.github.ai.split.api.response

import com.github.ai.split.api.ExpenseDto
import kotlinx.serialization.Serializable

@Serializable
data class PostExpenseResponse(
    val expense: ExpenseDto
)