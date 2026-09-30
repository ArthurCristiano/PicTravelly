package com.app.pictravelly.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.database.model.trip.TripWithSpots
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Transaction
    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    fun getAllTripsWithSpots(): Flow<List<TripWithSpots>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    fun getTripWithSpotsById(id: Long): Flow<TripWithSpots?>

    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripOnce(id: Long): TripEntity?

    // Retorna pontos turísticos que não pertencem a nenhuma viagem.
    @Transaction
    @Query("SELECT * FROM tourist_spots WHERE tripId IS NULL ORDER BY visitDate DESC")
    fun getSpotsWithoutTrip(): Flow<List<TouristSpotWithImages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTripById(id: Long)

    // Associa o ponto a uma viagem, ou desassocia se tripId for null.
    @Query("UPDATE tourist_spots SET tripId = :tripId WHERE id = :spotId")
    suspend fun assignSpotToTrip(spotId: Long, tripId: Long?)
}
