package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class TimestampDto(
    val timestampSeconds: Long,
    val formatted: String
)