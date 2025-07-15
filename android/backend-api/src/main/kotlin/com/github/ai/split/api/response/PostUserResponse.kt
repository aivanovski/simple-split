package com.github.ai.split.api.response

import kotlinx.serialization.Serializable
import com.github.ai.split.api.UserDto

@Serializable
data class PostUserResponse(
    val user: UserDto
)