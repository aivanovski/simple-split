package com.github.ai.simplesplit.android.model

import androidx.compose.runtime.Immutable

@Immutable
data class ErrorMessage(
    val message: String,
    val actionText: String,
    val actionId: Int? = null
)