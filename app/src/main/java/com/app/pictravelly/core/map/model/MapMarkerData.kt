package com.app.pictravelly.core.map.model

/**
 * Modelo agnóstico de dados para marcadores (pins) no mapa.
 */
data class MapMarkerData(
    val id: Long,
    val title: String,
    val snippet: String,
    val latitude: Double,
    val longitude: Double,
    val type: MarkerType = MarkerType.TOURIST_SPOT
)
