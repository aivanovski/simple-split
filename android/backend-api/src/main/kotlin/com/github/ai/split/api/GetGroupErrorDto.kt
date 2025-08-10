package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class GetGroupErrorDto(
    val uid: String,
    val message: String
)