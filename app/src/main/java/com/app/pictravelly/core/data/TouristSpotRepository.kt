package com.app.pictravelly.core.data

import com.app.pictravelly.core.database.model.touristSpot.SpotImageEntity
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotEntity
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import kotlinx.coroutines.flow.Flow

/**
 * Repositório que abstrai o acesso aos dados dos pontos turísticos e diários.
 */
interface TouristSpotRepository {
    fun getAllSpotsStream(): Flow<List<TouristSpotWithImages>>
    fun getSpotStream(id: Long): Flow<TouristSpotWithImages?>
    suspend fun getSpotOnce(id: Long): TouristSpotWithImages?
    fun searchSpotsStream(query: String): Flow<List<TouristSpotWithImages>>
    suspend fun insertSpotWithImages(spot: TouristSpotEntity, images: List<SpotImageEntity>): Long
    suspend fun updateSpotWithImages(spot: TouristSpotEntity, newImages: List<SpotImageEntity>)
    suspend fun deleteSpot(id: Long)
}
