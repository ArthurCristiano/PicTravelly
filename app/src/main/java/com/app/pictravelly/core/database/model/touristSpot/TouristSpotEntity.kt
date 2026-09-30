package com.app.pictravelly.core.database.model.touristSpot

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.pictravelly.core.database.model.trip.TripEntity

// Registros com tripId null representam pontos turísticos sem viagem associada.
@Entity(
    tableName = "tourist_spots",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["tripId"])]
)
data class TouristSpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long? = null,
    val title: String,
    val description: String,
    val locationName: String,
    val visitDate: Long, // Epoch millis
    val latitude: Double,
    val longitude: Double,
    val offlineMapSnapshotUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
