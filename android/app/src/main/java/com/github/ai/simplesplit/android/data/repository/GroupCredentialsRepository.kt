package com.github.ai.simplesplit.android.data.repository

import com.github.ai.simplesplit.android.data.dao.GroupCredentialsDao
import com.github.ai.simplesplit.android.model.GroupCredentials

class GroupCredentialsRepository(
    private val dao: GroupCredentialsDao
) {

    fun getAll(): List<GroupCredentials> {
        return dao.getAll()
    }

    fun add(credentials: GroupCredentials) {
        dao.insert(credentials)
    }
}