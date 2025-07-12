package com.github.ai.split.api.request

import kotlinx.serialization.Serializable

@Serializable
data class PostGroupRequest(
    val password: String,
    val title: String,
    val description: String?
)