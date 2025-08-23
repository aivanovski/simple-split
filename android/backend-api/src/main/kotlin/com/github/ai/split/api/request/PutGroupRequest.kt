package com.github.ai.split.api.request

import kotlinx.serialization.Serializable
import com.github.ai.split.api.UserUidDto

@Serializable
data class PutGroupRequest(
    val title: String?,
    val password: String?,
    val description: String?,
    val currencyIsoCode: String?,
    val members: List<UserUidDto>?
)