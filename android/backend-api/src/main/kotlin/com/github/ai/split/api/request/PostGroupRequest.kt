package com.github.ai.split.api.request

import com.github.ai.split.api.*
import kotlinx.serialization.Serializable

@Serializable
data class PostGroupRequest(
    val password: String,
    val title: String,
    val description: String?,
    val members: List<UserNameDto>?,
    val expenses: List<NewExpenseDto>?
)