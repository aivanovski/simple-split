package com.github.ai.split.api.request

data class PostGroupRequest(
    val password: String,
    val title: String,
    val description: String?
)