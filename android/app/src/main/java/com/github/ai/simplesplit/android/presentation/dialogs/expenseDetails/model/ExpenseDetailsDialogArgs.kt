package com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model

import com.github.ai.split.api.ExpenseDto
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDetailsDialogArgs(
    val expense: ExpenseDto
)