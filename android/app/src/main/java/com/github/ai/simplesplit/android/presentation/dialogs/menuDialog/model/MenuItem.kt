package com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model

import com.github.ai.simplesplit.android.presentation.core.compose.theme.Icon
import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val icon: Icon,
    val text: String,
    val actionId: Int
)