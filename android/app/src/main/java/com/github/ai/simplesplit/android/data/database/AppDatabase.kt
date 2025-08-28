package com.github.ai.simplesplit.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.ai.simplesplit.android.data.database.dao.CurrencyEntityDao
import com.github.ai.simplesplit.android.data.database.dao.GroupCredentialsDao
import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.simplesplit.android.data.database.model.GroupCredentials

@Database(
    entities = [
        GroupCredentials::class,
        CurrencyEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun groupCredentialsDao(): GroupCredentialsDao
    abstract fun currencyEntityDao(): CurrencyEntityDao

    companion object {

        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "simple-split.db"
            )
                .build()
        }
    }
}