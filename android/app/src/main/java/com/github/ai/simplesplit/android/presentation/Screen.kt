package com.github.ai.simplesplit.android.presentation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Groups : Screen()
}