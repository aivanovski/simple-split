package com.github.ai.simplesplit.android.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity("group_credentials")
data class GroupCredentials(
    @PrimaryKey
    val groupUid: String,
    // TODO: password should be hashed
    val password: String
)