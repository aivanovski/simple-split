package com.github.ai.simplesplit.android.presentation.screens.groupDetails.model

import com.github.ai.split.api.GroupDto
import kotlinx.serialization.Serializable

@Serializable
data class GroupDetailsArgs(
    val group: GroupDto,
    val password: String
)