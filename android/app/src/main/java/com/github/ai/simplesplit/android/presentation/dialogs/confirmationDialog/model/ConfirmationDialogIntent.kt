package com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class ConfirmationDialogIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : ConfirmationDialogIntent()
}