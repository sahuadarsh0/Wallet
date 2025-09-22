package com.technitedminds.wallet.data.mapper

import com.technitedminds.wallet.data.local.database.entities.CardEntity
import com.technitedminds.wallet.domain.model.Card

/** Mapper functions for converting between Card domain model and CardEntity data model. */

/** Converts CardEntity to Card domain model */
fun CardEntity.toDomainModel(): Card {
    return Card(
            id = id,
            name = name,
            type = type,
            categoryId = categoryId,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            extractedData = extractedData,
            customFields = customFields,
            createdAt = createdAt,
            updatedAt = updatedAt
    )
}

/** Converts Card domain model to CardEntity */
fun Card.toEntity(): CardEntity {
    return CardEntity(
            id = id,
            name = name,
            type = type,
            categoryId = categoryId,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            extractedData = extractedData,
            customFields = customFields,
            createdAt = createdAt,
            updatedAt = updatedAt
    )
}

/** Converts list of CardEntity to list of Card domain models */
fun List<CardEntity>.toDomainModels(): List<Card> {
    return map { it.toDomainModel() }
}

/** Converts list of Card domain models to list of CardEntity */
fun List<Card>.toEntities(): List<CardEntity> {
    return map { it.toEntity() }
}
