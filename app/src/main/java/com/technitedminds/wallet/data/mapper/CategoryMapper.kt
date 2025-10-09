package com.technitedminds.wallet.data.mapper

import com.technitedminds.wallet.data.local.database.entities.CategoryEntity
import com.technitedminds.wallet.domain.model.Category

/** Mapper functions for converting between Category domain model and CategoryEntity data model. */

/** Converts CategoryEntity to Category domain model */
fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        description = description,
        colorHex = colorHex,
        iconName = iconName,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/** Converts Category domain model to CategoryEntity */
fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        description = description,
        colorHex = colorHex,
        iconName = iconName ?: "category",
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/** Converts list of CategoryEntity to list of Category domain models */
fun List<CategoryEntity>.toDomainModels(): List<Category> {
    return map { it.toDomainModel() }
}

/** Converts list of Category domain models to list of CategoryEntity */
fun List<Category>.toEntities(): List<CategoryEntity> {
    return map { it.toEntity() }
}