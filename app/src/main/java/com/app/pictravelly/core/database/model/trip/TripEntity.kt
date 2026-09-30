package com.app.pictravelly.core.database.model.trip

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startDate: Long, // Epoch millis
    val endDate: Long? = null, // Null quando a viagem ainda está em andamento
    val coverImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
