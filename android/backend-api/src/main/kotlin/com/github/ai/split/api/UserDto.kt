package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val uid: String,
    val name: String
)