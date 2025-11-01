package com.technitedminds.wallet.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.technitedminds.wallet.data.local.database.converters.CardTypeConverter
import com.technitedminds.wallet.data.local.database.converters.MapConverter
import com.technitedminds.wallet.data.local.database.converters.CardGradientConverter
import com.technitedminds.wallet.data.local.database.dao.CardDao
import com.technitedminds.wallet.data.local.database.dao.CategoryDao
import com.technitedminds.wallet.data.local.database.entities.CardEntity
import com.technitedminds.wallet.data.local.database.entities.CategoryEntity

/**
 * Room database for the CardVault wallet application. Manages local storage of cards and categories
 * with proper configuration and migrations.
 */
@Database(entities = [CardEntity::class, CategoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(CardTypeConverter::class, MapConverter::class, CardGradientConverter::class)
abstract class WalletDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "wallet_database"

        @Volatile private var INSTANCE: WalletDatabase? = null

        fun getDatabase(context: Context): WalletDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                WalletDatabase::class.java,
                                                DATABASE_NAME
                                        )
                                        .fallbackToDestructiveMigration()
                                        .addCallback(DatabaseCallback())
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }

        /** Database callback for initialization tasks */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database creation tasks can be added here
                // For example, inserting default categories
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Tasks to run every time database is opened
            }
        }



        /** Clears the database instance (useful for testing) */
        fun clearInstance() {
            INSTANCE = null
        }
        
        /** Clears all data from the database (resets to new user state) */
        suspend fun clearAllData(context: Context) {
            val database = getDatabase(context)
            database.clearAllTables()
        }
    }
}
