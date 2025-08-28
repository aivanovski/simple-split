package com.github.ai.simplesplit.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity

@Dao
interface CurrencyEntityDao {

    @Query("SELECT * FROM currencies WHERE isoCode = :isoCode")
    fun findByIsoCode(isoCode: String): CurrencyEntity?

    @Query("SELECT * FROM currencies")
    fun getAll(): List<CurrencyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currency: CurrencyEntity)

    @Update
    fun update(currency: CurrencyEntity)

    @Query("DELETE FROM currencies WHERE isoCode = :isoCode")
    fun deleteByIsoCode(isoCode: String)
}