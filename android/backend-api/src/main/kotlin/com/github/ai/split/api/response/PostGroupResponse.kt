package com.github.ai.split.api.response

import com.github.ai.split.api.GroupDto
import kotlinx.serialization.Serializable

@Serializable
data class PostGroupResponse(
    val group: GroupDto
)