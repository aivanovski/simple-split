package com.github.ai.simplesplit.android.presentation.screens.root.model

sealed interface RootIntent {
    data object OnBackClick : RootIntent
}