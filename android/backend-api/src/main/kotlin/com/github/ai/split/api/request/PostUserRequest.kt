package com.github.ai.split.api.request

import kotlinx.serialization.Serializable

@Serializable
data class PostUserRequest(
    val name: String,
    val password: String
)