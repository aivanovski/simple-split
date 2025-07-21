package com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuDialogArgs(
    val items: List<MenuItem>
)