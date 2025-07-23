package com.github.ai.split.api.response

import kotlinx.serialization.Serializable
import com.github.ai.split.api.ExpenseDto

@Serializable
data class PutExpenseResponse(
    val expense: ExpenseDto
)