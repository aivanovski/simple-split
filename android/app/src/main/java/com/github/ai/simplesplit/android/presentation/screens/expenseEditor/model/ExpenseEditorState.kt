package com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model

import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.utils.StringUtils

sealed interface ExpenseEditorState {
    data object Loading : ExpenseEditorState

    data class Error(
        val error: ErrorMessage
    ) : ExpenseEditorState

    data class Data(
        val payer: String = StringUtils.EMPTY,
        val title: String = StringUtils.EMPTY,
        val amount: String = StringUtils.EMPTY,
        val availablePayers: List<String> = emptyList(),
        val titleError: String? = null,
        val amountError: String? = null,
        val error: ErrorMessage? = null
    ) : ExpenseEditorState
}