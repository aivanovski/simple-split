package com.github.ai.simplesplit.android.presentation.screens.groupDetails.model

import com.github.ai.simplesplit.android.model.Group
import kotlinx.serialization.Serializable

@Serializable
data class GroupDetailsArgs(
    val group: Group,
    val password: String
)