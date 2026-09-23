package com.app.pictravelly.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade de representação de um Ponto Turístico no Room Database.
 * Atende aos requisitos RF01, RF03, RF05 e modelagem de docs/DATABASE.md.
 */
@Entity(tableName = "tourist_spots")
data class TouristSpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val locationName: String,
    val visitDate: Long, // Epoch millis
    val latitude: Double,
    val longitude: Double,
    val offlineMapSnapshotUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
