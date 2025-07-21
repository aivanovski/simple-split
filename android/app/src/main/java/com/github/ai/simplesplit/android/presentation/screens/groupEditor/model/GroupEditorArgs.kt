package com.github.ai.simplesplit.android.presentation.screens.groupEditor.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupEditorArgs(
    val mode: GroupEditorMode
)