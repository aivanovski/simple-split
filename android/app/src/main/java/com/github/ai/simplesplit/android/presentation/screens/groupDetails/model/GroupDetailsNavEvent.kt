package com.github.ai.simplesplit.android.presentation.screens.groupDetails.model

sealed interface GroupDetailsNavEvent {
    data class OpenUrl(val url: String) : GroupDetailsNavEvent
    data class ShareUrl(val url: String) : GroupDetailsNavEvent
}