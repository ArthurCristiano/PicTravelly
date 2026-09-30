package com.app.pictravelly.core.database.model.touristSpot

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Modelo de relação 1:N entre TouristSpot e SpotImages.
 */
data class TouristSpotWithImages(
    @Embedded
    val spot: TouristSpotEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "spotId"
    )
    val images: List<SpotImageEntity> = emptyList()
) {
    /**
     * Retorna a URI da imagem de capa (se houver alguma marcada como capa)
     * ou da primeira imagem da lista.
     */
    val coverImageUri: String?
        get() = images.firstOrNull { it.isCover }?.imageUri ?: images.firstOrNull()?.imageUri
}
