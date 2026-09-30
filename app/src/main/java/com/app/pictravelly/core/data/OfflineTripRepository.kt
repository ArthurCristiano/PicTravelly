package com.app.pictravelly.core.data

import com.app.pictravelly.core.database.dao.TripDao
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.database.model.trip.TripWithSpots
import kotlinx.coroutines.flow.Flow

/**
 * Implementação offline do repositório de viagens consumindo o Room DAO.
 */
class OfflineTripRepository(
    private val tripDao: TripDao
) : TripRepository {

    override fun getAllTripsStream(): Flow<List<TripWithSpots>> {
        return tripDao.getAllTripsWithSpots()
    }

    override fun getTripStream(id: Long): Flow<TripWithSpots?> {
        return tripDao.getTripWithSpotsById(id)
    }

    override fun getTripsForPickerStream(): Flow<List<TripEntity>> {
        return tripDao.getAllTrips()
    }

    override fun getSpotsWithoutTripStream(): Flow<List<TouristSpotWithImages>> {
        return tripDao.getSpotsWithoutTrip()
    }

    override suspend fun getTripOnce(id: Long): TripEntity? {
        return tripDao.getTripOnce(id)
    }

    override suspend fun insertTrip(trip: TripEntity): Long {
        return tripDao.insertTrip(trip)
    }

    override suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
    }

    override suspend fun deleteTrip(id: Long) {
        tripDao.deleteTripById(id)
    }

    override suspend fun assignSpotToTrip(spotId: Long, tripId: Long?) {
        tripDao.assignSpotToTrip(spotId, tripId)
    }
}
