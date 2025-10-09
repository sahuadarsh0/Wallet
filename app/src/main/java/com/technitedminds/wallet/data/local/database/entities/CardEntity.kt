package com.technitedminds.wallet.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.technitedminds.wallet.data.local.database.converters.CardTypeConverter
import com.technitedminds.wallet.data.local.database.converters.MapConverter
import com.technitedminds.wallet.data.local.database.converters.CardGradientConverter
import com.technitedminds.wallet.domain.model.CardType

/**
 * Room entity representing a card in the database. Maps to the Card domain model with proper Room
 * annotations.
 */
@Entity(
        tableName = "cards",
        foreignKeys =
                [
                        ForeignKey(
                                entity = CategoryEntity::class,
                                parentColumns = ["id"],
                                childColumns = ["category_id"],
                                onDelete = ForeignKey.CASCADE
                        )],
        indices =
                [
                        Index(value = ["category_id"]),
                        Index(value = ["name"]),
                        Index(value = ["created_at"]),
                        Index(value = ["updated_at"])]
)
@TypeConverters(CardTypeConverter::class, MapConverter::class, CardGradientConverter::class)
data class CardEntity(
        @PrimaryKey @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "type") val type: CardType,
        @ColumnInfo(name = "category_id") val categoryId: String,
        @ColumnInfo(name = "front_image_path") val frontImagePath: String,
        @ColumnInfo(name = "back_image_path") val backImagePath: String,
        @ColumnInfo(name = "extracted_data") val extractedData: Map<String, String>,
        @ColumnInfo(name = "custom_fields") val customFields: Map<String, String>,
        @ColumnInfo(name = "expiry_date") val expiryDate: String?,
        @ColumnInfo(name = "notes") val notes: String?,
        @ColumnInfo(name = "custom_gradient") val customGradient: com.technitedminds.wallet.domain.model.CardGradient?,
        @ColumnInfo(name = "created_at") val createdAt: Long,
        @ColumnInfo(name = "updated_at") val updatedAt: Long
)
