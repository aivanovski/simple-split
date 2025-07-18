package com.github.ai.simplesplit.android.presentation.screens.groups.model

import com.github.ai.simplesplit.android.model.db.GroupCredentials
import com.github.ai.split.api.GroupDto

data class GroupsData(
    val groups: List<GroupDto> = emptyList(),
    val credentials: List<GroupCredentials> = emptyList()
)