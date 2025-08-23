package com.github.ai.split.api.request

import kotlinx.serialization.Serializable
import com.github.ai.split.api.NewExpenseDto
import com.github.ai.split.api.UserNameDto

@Serializable
data class PostGroupRequest(
    val password: String,
    val title: String,
    val description: String?,
    val currencyIsoCode: String,
    val members: List<UserNameDto>?,
    val expenses: List<NewExpenseDto>?
)