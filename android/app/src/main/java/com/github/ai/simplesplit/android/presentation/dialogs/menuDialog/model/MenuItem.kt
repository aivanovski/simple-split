package com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model

import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val icon: AppIcon,
    val text: String,
    val actionId: Int
)