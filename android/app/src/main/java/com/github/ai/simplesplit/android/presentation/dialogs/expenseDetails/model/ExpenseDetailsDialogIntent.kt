package com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class ExpenseDetailsDialogIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : ExpenseDetailsDialogIntent()
    data object Dismiss : ExpenseDetailsDialogIntent()
    data object OnEditMenuClick : ExpenseDetailsDialogIntent()
    data object OnRemoveMenuClick : ExpenseDetailsDialogIntent()
}