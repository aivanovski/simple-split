package com.github.ai.simplesplit.android.data.repository

import com.github.ai.simplesplit.android.data.database.dao.GroupCredentialsDao
import com.github.ai.simplesplit.android.model.db.GroupCredentials
import kotlinx.coroutines.flow.Flow

class GroupCredentialsRepository(
    private val dao: GroupCredentialsDao
) {

    fun getAll(): List<GroupCredentials> = dao.getAll()

    fun getAllFlow(): Flow<List<GroupCredentials>> = dao.getAllFlow()

    fun getByGroupUid(groupUid: String): GroupCredentials? = dao.getByGroupUid(groupUid)

    fun removeByGroupUid(groupUid: String): Unit = dao.deleteByGroupUid(groupUid)

    fun add(credentials: GroupCredentials) = dao.insert(credentials)
}