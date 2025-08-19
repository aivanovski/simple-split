package com.github.ai.simplesplit.android.presentation.screens.groupEditor.model

import androidx.compose.runtime.Immutable

@Immutable
data class MemberItem(
    val name: String,
    val uid: String? = null
)