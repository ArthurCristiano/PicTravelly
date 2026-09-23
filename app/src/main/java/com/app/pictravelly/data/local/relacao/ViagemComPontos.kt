package com.app.pictravelly.data.local.relacao

import androidx.room.Embedded
import androidx.room.Relation
import com.app.pictravelly.data.local.entidade.Ponto
import com.app.pictravelly.data.local.entidade.Viagem

/** Uma viagem junto de todos os pontos turisticos cadastrados nela. */
data class ViagemComPontos(
    @Embedded
    val viagem: Viagem,

    @Relation(
        parentColumn = "id",
        entityColumn = "viagem_id"
    )
    val pontos: List<Ponto>
)
