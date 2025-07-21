package com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseEditorArgs(
    val mode: ExpenseEditorMode
)