package com.app.pictravelly.core.map.model

/**
 * Representa um marcador exibido no mapa.
 */
data class MapMarkerData(
    val id: Long,
    val title: String,
    val snippet: String,
    val latitude: Double,
    val longitude: Double,
    val type: MarkerType = MarkerType.TOURIST_SPOT
)
