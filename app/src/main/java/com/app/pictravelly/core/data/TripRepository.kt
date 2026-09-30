package com.app.pictravelly.core.data

import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.database.model.trip.TripWithSpots
import kotlinx.coroutines.flow.Flow

/**
 * Repositório que abstrai o acesso às viagens e ao seu agrupamento de pontos.
 */
interface TripRepository {
    fun getAllTripsStream(): Flow<List<TripWithSpots>>
    fun getTripStream(id: Long): Flow<TripWithSpots?>
    fun getTripsForPickerStream(): Flow<List<TripEntity>>
    fun getSpotsWithoutTripStream(): Flow<List<TouristSpotWithImages>>
    suspend fun getTripOnce(id: Long): TripEntity?
    suspend fun insertTrip(trip: TripEntity): Long
    suspend fun updateTrip(trip: TripEntity)
    suspend fun deleteTrip(id: Long)
    suspend fun assignSpotToTrip(spotId: Long, tripId: Long?)
}
