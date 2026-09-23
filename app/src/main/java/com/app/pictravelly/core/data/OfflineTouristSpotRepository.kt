package com.app.pictravelly.core.data

import com.app.pictravelly.core.database.dao.TouristSpotDao
import com.app.pictravelly.core.database.model.SpotImageEntity
import com.app.pictravelly.core.database.model.TouristSpotEntity
import com.app.pictravelly.core.database.model.TouristSpotWithImages
import kotlinx.coroutines.flow.Flow

/**
 * Implementação offline do repositório consumindo o Room DAO.
 */
class OfflineTouristSpotRepository(
    private val touristSpotDao: TouristSpotDao
) : TouristSpotRepository {

    override fun getAllSpotsStream(): Flow<List<TouristSpotWithImages>> {
        return touristSpotDao.getAllSpotsWithImages()
    }

    override fun getSpotStream(id: Long): Flow<TouristSpotWithImages?> {
        return touristSpotDao.getSpotWithImagesById(id)
    }

    override suspend fun getSpotOnce(id: Long): TouristSpotWithImages? {
        return touristSpotDao.getSpotWithImagesByIdOnce(id)
    }

    override fun searchSpotsStream(query: String): Flow<List<TouristSpotWithImages>> {
        return if (query.isBlank()) {
            touristSpotDao.getAllSpotsWithImages()
        } else {
            touristSpotDao.searchSpots(query.trim())
        }
    }

    override suspend fun insertSpotWithImages(
        spot: TouristSpotEntity,
        images: List<SpotImageEntity>
    ): Long {
        return touristSpotDao.insertSpotWithImages(spot, images)
    }

    override suspend fun updateSpotWithImages(
        spot: TouristSpotEntity,
        newImages: List<SpotImageEntity>
    ) {
        touristSpotDao.updateSpotWithImages(spot, newImages)
    }

    override suspend fun deleteSpot(id: Long) {
        touristSpotDao.deleteSpotById(id)
    }
}
