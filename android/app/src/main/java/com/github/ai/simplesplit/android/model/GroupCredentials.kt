package com.github.ai.simplesplit.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("group_credentials")
data class GroupCredentials(
    @PrimaryKey
    val groupUid: String,
    val password: String
)