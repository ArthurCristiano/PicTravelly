package com.app.pictravelly.core.database.model.trip

import androidx.room.Embedded
import androidx.room.Relation
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotEntity
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages

data class TripWithSpots(
    @Embedded
    val trip: TripEntity,

    @Relation(
        entity = TouristSpotEntity::class,
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val spots: List<TouristSpotWithImages> = emptyList()
) {
    val spotsCount: Int
        get() = spots.size

    // Retorna a capa da viagem ou a primeira foto disponível de seus pontos.
    val displayCoverUri: String?
        get() = trip.coverImageUri ?: spots.firstNotNullOfOrNull { it.coverImageUri }
}
