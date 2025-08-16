package com.github.ai.simplesplit.android.presentation.screens.groups.model

import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.split.api.GroupDto

data class GroupsData(
    val groups: List<GroupDto> = emptyList(),
    val requestedCredentials: List<GroupCredentials> = emptyList()
)