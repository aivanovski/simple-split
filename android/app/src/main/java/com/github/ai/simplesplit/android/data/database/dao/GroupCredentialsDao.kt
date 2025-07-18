package com.github.ai.simplesplit.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.ai.simplesplit.android.model.db.GroupCredentials
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupCredentialsDao {

    @Query("SELECT * FROM group_credentials WHERE groupUid = :groupUid")
    fun getByGroupUid(groupUid: String): GroupCredentials?

    @Query("SELECT * FROM group_credentials")
    fun getAll(): List<GroupCredentials>

    @Query("SELECT * FROM group_credentials")
    fun getAllFlow(): Flow<List<GroupCredentials>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(credentials: GroupCredentials)

    @Update
    fun update(credentials: GroupCredentials)

    @Query("DELETE FROM group_credentials WHERE groupUid = :groupUid")
    fun deleteByGroupUid(groupUid: String)
}