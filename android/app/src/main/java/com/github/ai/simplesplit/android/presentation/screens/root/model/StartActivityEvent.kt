package com.github.ai.simplesplit.android.presentation.screens.root.model

sealed interface StartActivityEvent {
    data class OpenUrl(val url: String) : StartActivityEvent
    data class ShareUrl(val url: String) : StartActivityEvent
}