package com.technitedminds.wallet.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a category in the database. Maps to the Category domain model with
 * proper Room annotations.
 */
@Entity(
        tableName = "categories",
        indices =
                [
                        Index(value = ["name"], unique = true),
                        Index(value = ["is_default"]),
                        Index(value = ["created_at"])]
)
data class CategoryEntity(
        @PrimaryKey @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "icon_res_id") val iconResId: Int,
        @ColumnInfo(name = "color_hex") val colorHex: String,
        @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
        @ColumnInfo(name = "created_at") val createdAt: Long,
        @ColumnInfo(name = "updated_at") val updatedAt: Long
)
