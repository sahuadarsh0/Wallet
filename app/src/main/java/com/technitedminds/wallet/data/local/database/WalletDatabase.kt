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
@Database(entities = [CardEntity::class, CategoryEntity::class], version = 3, exportSchema = true)
@TypeConverters(CardTypeConverter::class, MapConverter::class, CardGradientConverter::class)
abstract class WalletDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "wallet_database"

        @Volatile private var INSTANCE: WalletDatabase? = null

        /**
         * Migration v1 → v2: Replace the predefined "personal" category with "government".
         *
         * Steps performed atomically:
         *   1. Ensure a "government" row exists in [categories] (insert if missing).
         *   2. Reassign every card whose [category_id] is "personal" to "government".
         *   3. Delete the obsolete "personal" category row.
         *
         * Cards must be reassigned BEFORE deleting "personal" because the cards table
         * declares ON DELETE CASCADE on its category_id foreign key.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()

                db.execSQL(
                    """
                    INSERT OR IGNORE INTO categories
                        (id, name, description, color_hex, icon_name, sort_order, created_at, updated_at)
                    VALUES
                        ('government', 'Government', 'Government-issued IDs and documents',
                         '#1976D2', 'AccountBalance', 0, ?, ?)
                    """.trimIndent(),
                    arrayOf(now, now)
                )

                db.execSQL(
                    "UPDATE cards SET category_id = 'government' WHERE category_id = 'personal'"
                )

                db.execSQL("DELETE FROM categories WHERE id = 'personal'")
            }
        }

        /**
         * Migration v2 → v3: Seed the [Category.DEFAULT] ("default" / "General") row.
         *
         * The "General" option in pickers maps to `Category.DEFAULT.id` ("default").
         * Because [CardEntity.category_id] has a foreign key to [categories], every
         * referenced category id MUST exist as a row — otherwise inserting a card
         * with `categoryId = "default"` fails with a FK constraint violation
         * (surfaced to users as "Failed to save card: ..."). This migration:
         *
         *   1. Inserts the "default" category if it doesn't already exist.
         *   2. Reassigns any cards whose category_id is empty/blank to "default"
         *      (defensive — older builds may have allowed blank category ids).
         */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()

                db.execSQL(
                    """
                    INSERT OR IGNORE INTO categories
                        (id, name, description, color_hex, icon_name, sort_order, created_at, updated_at)
                    VALUES
                        ('default', 'General', 'Default category for cards',
                         '#1976D2', 'Category', 0, ?, ?)
                    """.trimIndent(),
                    arrayOf(now, now)
                )

                db.execSQL(
                    "UPDATE cards SET category_id = 'default' WHERE TRIM(category_id) = ''"
                )
            }
        }

        fun getDatabase(context: Context): WalletDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                WalletDatabase::class.java,
                                                DATABASE_NAME
                                        )
                                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
                val now = System.currentTimeMillis()
                // Seed the "default" / "General" category so that cards saved
                // under the General bucket satisfy the cards.category_id FK.
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO categories
                        (id, name, description, color_hex, icon_name, sort_order, created_at, updated_at)
                    VALUES
                        ('default', 'General', 'Default category for cards',
                         '#1976D2', 'Category', 0, ?, ?)
                    """.trimIndent(),
                    arrayOf(now, now)
                )
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

        /** Secure wipe for database tables and physical database files. */
        suspend fun wipeCompletely(context: Context) {
            val database = getDatabase(context)
            database.clearAllTables()
            database.close()
            clearInstance()
            context.deleteDatabase(DATABASE_NAME)
            context.deleteDatabase("$DATABASE_NAME-wal")
            context.deleteDatabase("$DATABASE_NAME-shm")
            context.deleteDatabase("$DATABASE_NAME-journal")
        }
    }
}
