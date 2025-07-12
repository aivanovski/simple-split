package com.github.ai.split.api.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val userUid: String
)