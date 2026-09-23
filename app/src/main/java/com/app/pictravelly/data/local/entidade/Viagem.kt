package com.app.pictravelly.data.local.entidade

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Uma viagem do diario. E o "card" que aparece na tela inicial e
 * funciona como pasta para os pontos turisticos visitados nela.
 */
@Entity(tableName = "viagens")
data class Viagem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val titulo: String,

    val descricao: String = "",

    /** Data de inicio em milissegundos (epoch). */
    @ColumnInfo(name = "data_inicio")
    val dataInicio: Long,

    /** Data de termino em milissegundos. Nulo enquanto a viagem esta em andamento. */
    @ColumnInfo(name = "data_fim")
    val dataFim: Long? = null,

    /** Caminho absoluto da imagem de capa no armazenamento interno do app. */
    @ColumnInfo(name = "caminho_capa")
    val caminhoCapa: String? = null,

    @ColumnInfo(name = "criado_em")
    val criadoEm: Long = System.currentTimeMillis()
)
