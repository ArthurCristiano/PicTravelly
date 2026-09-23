package com.app.pictravelly.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade de imagem atrelada a um ponto turístico com exclusão em cascata.
 */
@Entity(
    tableName = "spot_images",
    foreignKeys = [
        ForeignKey(
            entity = TouristSpotEntity::class,
            parentColumns = ["id"],
            childColumns = ["spotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spotId"])]
)
data class SpotImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val spotId: Long,
    val imageUri: String,
    val isCover: Boolean = false
)
