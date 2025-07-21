package com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class ExpenseEditorIntent(
    override val isImmediate: Boolean = false
) : MviIntent {

    data object Initialize : ExpenseEditorIntent()
    data object OnBackClick : ExpenseEditorIntent()
    data object OnDoneClick : ExpenseEditorIntent()
    data class OnPayerChanged(val payer: String) : ExpenseEditorIntent(isImmediate = true)
    data class OnTitleChanged(val title: String) : ExpenseEditorIntent(isImmediate = true)
    data class OnAmountChanged(val amount: String) : ExpenseEditorIntent(isImmediate = true)
}