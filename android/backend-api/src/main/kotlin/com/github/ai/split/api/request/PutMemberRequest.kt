package com.github.ai.split.api.request

import kotlinx.serialization.Serializable

@Serializable
data class PutMemberRequest(
    val name: String
)