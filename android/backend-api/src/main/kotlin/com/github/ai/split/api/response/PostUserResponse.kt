package com.github.ai.split.api.response

import com.github.ai.split.api.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class PostUserResponse(
    val user: UserDto
)