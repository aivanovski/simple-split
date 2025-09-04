package com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model

import com.github.ai.simplesplit.android.model.Expense
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDetailsDialogArgs(
    val expense: Expense
)