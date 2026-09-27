package com.app.pictravelly.core.database.model.trip

import androidx.room.Embedded
import androidx.room.Relation
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotEntity
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages

/**
 * Modelo de relação entre Trip e seus pontos turísticos.
 *
 * Usa relação em dois níveis: a viagem traz os pontos e cada ponto já traz as
 * suas imagens, então a listagem e o detalhe da viagem são resolvidos em uma
 * única consulta.
 */
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

    /**
     * Capa escolhida pelo usuário ou, na falta dela, a primeira foto
     * encontrada entre os pontos da viagem.
     */
    val displayCoverUri: String?
        get() = trip.coverImageUri ?: spots.firstNotNullOfOrNull { it.coverImageUri }
}
