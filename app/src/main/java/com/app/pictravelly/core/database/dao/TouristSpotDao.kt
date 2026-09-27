package com.app.pictravelly.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.pictravelly.core.database.model.touristSpot.SpotImageEntity
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotEntity
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para TouristSpot e SpotImages.
 */
@Dao
interface TouristSpotDao {

    @Transaction
    @Query("SELECT * FROM tourist_spots ORDER BY visitDate DESC")
    fun getAllSpotsWithImages(): Flow<List<TouristSpotWithImages>>

    @Transaction
    @Query("SELECT * FROM tourist_spots WHERE id = :id LIMIT 1")
    fun getSpotWithImagesById(id: Long): Flow<TouristSpotWithImages?>

    @Transaction
    @Query("SELECT * FROM tourist_spots WHERE id = :id LIMIT 1")
    suspend fun getSpotWithImagesByIdOnce(id: Long): TouristSpotWithImages?

    @Transaction
    @Query("""
        SELECT * FROM tourist_spots 
        WHERE title LIKE '%' || :query || '%' 
           OR locationName LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
        ORDER BY visitDate DESC
    """)
    fun searchSpots(query: String): Flow<List<TouristSpotWithImages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: TouristSpotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<SpotImageEntity>)

    @Update
    suspend fun updateSpot(spot: TouristSpotEntity)

    @Delete
    suspend fun deleteSpot(spot: TouristSpotEntity)

    @Query("DELETE FROM tourist_spots WHERE id = :id")
    suspend fun deleteSpotById(id: Long)

    @Query("DELETE FROM spot_images WHERE spotId = :spotId")
    suspend fun deleteImagesBySpotId(spotId: Long)

    @Transaction
    suspend fun insertSpotWithImages(spot: TouristSpotEntity, images: List<SpotImageEntity>): Long {
        val spotId = insertSpot(spot)
        val imagesWithSpotId = images.map { it.copy(spotId = spotId) }
        insertImages(imagesWithSpotId)
        return spotId
    }

    @Transaction
    suspend fun updateSpotWithImages(spot: TouristSpotEntity, newImages: List<SpotImageEntity>) {
        updateSpot(spot)
        deleteImagesBySpotId(spot.id)
        val imagesWithSpotId = newImages.map { it.copy(spotId = spot.id) }
        insertImages(imagesWithSpotId)
    }
}
