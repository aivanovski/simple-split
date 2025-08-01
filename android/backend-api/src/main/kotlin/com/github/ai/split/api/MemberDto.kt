package com.github.ai.split.api

import kotlinx.serialization.Serializable

@Serializable
data class MemberDto(
    val uid: String,
    val name: String
)