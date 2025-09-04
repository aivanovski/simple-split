package com.github.ai.simplesplit.android.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val uid: String,
    val name: String
)