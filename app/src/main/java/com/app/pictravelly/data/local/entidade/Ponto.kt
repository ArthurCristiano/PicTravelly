package com.app.pictravelly.data.local.entidade

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Um ponto turistico cadastrado dentro de uma viagem: geocodigos,
 * nome, descricao e imagem. Apagar a viagem apaga os pontos em cascata.
 */
@Entity(
    tableName = "pontos",
    foreignKeys = [
        ForeignKey(
            entity = Viagem::class,
            parentColumns = ["id"],
            childColumns = ["viagem_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("viagem_id")]
)
data class Ponto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "viagem_id")
    val viagemId: Long,

    val nome: String,

    val descricao: String = "",

    val latitude: Double,

    val longitude: Double,

    /**
     * Endereco textual resolvido pela API de geocodificacao reversa.
     * Fica gravado no banco para que o app continue mostrando o endereco
     * mesmo sem conexao depois do primeiro cadastro.
     */
    @ColumnInfo(name = "endereco")
    val endereco: String? = null,

    /** Caminho absoluto da foto no armazenamento interno do app. */
    @ColumnInfo(name = "caminho_imagem")
    val caminhoImagem: String? = null,

    @ColumnInfo(name = "criado_em")
    val criadoEm: Long = System.currentTimeMillis()
)
