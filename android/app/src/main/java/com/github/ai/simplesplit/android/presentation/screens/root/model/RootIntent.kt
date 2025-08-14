package com.github.ai.simplesplit.android.presentation.screens.root.model

sealed interface RootIntent {
    data object OnBackClick : RootIntent
    data class StartActivity(
        val event:
        com.github.ai.simplesplit.android.presentation.screens.root.model.StartActivityEvent
    ) : RootIntent
}