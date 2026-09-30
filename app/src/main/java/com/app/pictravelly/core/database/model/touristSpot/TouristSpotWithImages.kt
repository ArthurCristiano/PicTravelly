package com.app.pictravelly.core.database.model.touristSpot

import androidx.room.Embedded
import androidx.room.Relation

data class TouristSpotWithImages(
    @Embedded
    val spot: TouristSpotEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "spotId"
    )
    val images: List<SpotImageEntity> = emptyList()
) {
    // Retorna a imagem marcada como capa ou a primeira disponível.
    val coverImageUri: String?
        get() = images.firstOrNull { it.isCover }?.imageUri ?: images.firstOrNull()?.imageUri
}
