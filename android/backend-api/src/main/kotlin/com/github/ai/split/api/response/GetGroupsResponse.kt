package com.github.ai.split.api.response

import kotlinx.serialization.Serializable
import com.github.ai.split.api.GroupDto

@Serializable
data class GetGroupsResponse(
    val groups: List<GroupDto>
)