package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessageDto(
    val message: String?,
    val stacktrace: String
)