package com.github.ai.split.api.request

import kotlinx.serialization.Serializable

@Serializable
data class PostMemberRequest(
    val groupUid: String,
    val name: String
)