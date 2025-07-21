package com.github.ai.split.api.request

import kotlinx.serialization.Serializable
import com.github.ai.split.api.NewExpenseDto
import com.github.ai.split.api.UserDto
import com.github.ai.split.api.UserNameDto
import com.github.ai.split.api.UserUid

@Serializable
data class PostGroupRequest(
    val password: String,
    val title: String,
    val description: String?,
    val members: List<UserNameDto>?,
    val expenses: List<NewExpenseDto>?
)