package com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ExpenseEditorMode {
    @Serializable
    data object NewExpense : ExpenseEditorMode()

    @Serializable
    data class EditExpense(
        val expenseUid: String
    ) : ExpenseEditorMode()
}