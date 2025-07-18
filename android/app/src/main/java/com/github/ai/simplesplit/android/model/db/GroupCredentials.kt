package com.github.ai.simplesplit.android.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("group_credentials")
data class GroupCredentials(
    @PrimaryKey
    val groupUid: String,
    val password: String
)