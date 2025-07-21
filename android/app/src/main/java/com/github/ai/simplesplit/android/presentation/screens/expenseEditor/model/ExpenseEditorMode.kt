package com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model

import com.github.ai.split.api.GroupDto
import kotlinx.serialization.Serializable

@Serializable
sealed class ExpenseEditorMode {
    @Serializable
    data class NewExpense(
        val group: GroupDto
    ) : ExpenseEditorMode()
}