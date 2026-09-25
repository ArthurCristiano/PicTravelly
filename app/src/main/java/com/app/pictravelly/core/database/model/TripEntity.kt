package com.app.pictravelly.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade de representação de uma Viagem no Room Database.
 *
 * Uma viagem agrupa vários pontos turísticos e é o "card" exibido na aba Diário.
 * O vínculo é opcional: um ponto pode existir sem pertencer a nenhuma viagem.
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startDate: Long, // Epoch millis
    val endDate: Long? = null, // Nulo enquanto a viagem está em andamento
    val coverImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
