package com.github.ai.simplesplit.android.presentation.screens.settings.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class SettingsIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : SettingsIntent()
    data object OnBackClick : SettingsIntent()
}