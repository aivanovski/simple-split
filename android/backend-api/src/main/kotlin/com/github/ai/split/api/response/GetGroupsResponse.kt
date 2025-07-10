package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto

data class GetGroupsResponse(
    val groups: List<GroupDto>
)