package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupsResponse(
    val groups: List<GroupDto>
)