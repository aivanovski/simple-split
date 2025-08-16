package com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model

import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.split.api.GroupDto
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseEditorArgs(
    val mode: ExpenseEditorMode,
    val group: GroupDto,
    val credentials: GroupCredentials
)