package com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class MenuDialogIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : MenuDialogIntent()
}