package com.technitedminds.wallet.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
@Database(entities = [CardEntity::class, CategoryEntity::class], version = 2, exportSchema = false)
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
                                        .addMigrations(
                                                MIGRATION_1_2
                                                )
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

        /** Migration from version 1 to 2 - Update schema for cards and categories */
        val MIGRATION_1_2 =
                object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        // Add new columns to cards table
                        database.execSQL("ALTER TABLE cards ADD COLUMN expiry_date TEXT")
                        database.execSQL("ALTER TABLE cards ADD COLUMN notes TEXT")
                        database.execSQL("ALTER TABLE cards ADD COLUMN custom_gradient TEXT")
                        
                        // Update categories table schema
                        database.execSQL("""
                            CREATE TABLE categories_new (
                                id TEXT PRIMARY KEY NOT NULL,
                                name TEXT NOT NULL,
                                color_hex TEXT NOT NULL,
                                icon_name TEXT NOT NULL,
                                sort_order INTEGER NOT NULL,
                                created_at INTEGER NOT NULL,
                                updated_at INTEGER NOT NULL
                            )
                        """)
                        
                        // Migrate existing category data if any exists
                        database.execSQL("""
                            INSERT INTO categories_new (id, name, color_hex, icon_name, sort_order, created_at, updated_at)
                            SELECT id, name, color_hex, 'category', 0, created_at, updated_at FROM categories
                        """)
                        
                        // Drop old table and rename new one
                        database.execSQL("DROP TABLE categories")
                        database.execSQL("ALTER TABLE categories_new RENAME TO categories")
                        
                        // Create indices for categories
                        database.execSQL("CREATE UNIQUE INDEX index_categories_name ON categories(name)")
                        database.execSQL("CREATE INDEX index_categories_sort_order ON categories(sort_order)")
                        database.execSQL("CREATE INDEX index_categories_created_at ON categories(created_at)")
                    }
                }

        /** Clears the database instance (useful for testing) */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
