package com.app.pictravelly.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.pictravelly.data.local.entidade.Ponto
import kotlinx.coroutines.flow.Flow

@Dao
interface PontoDao {

    /** Todos os pontos de todas as viagens: alimenta a tela de Mapa. */
    @Query("SELECT * FROM pontos ORDER BY criado_em DESC")
    fun observarTodos(): Flow<List<Ponto>>

    @Query("SELECT * FROM pontos WHERE viagem_id = :viagemId ORDER BY criado_em ASC")
    fun observarDaViagem(viagemId: Long): Flow<List<Ponto>>

    @Query("SELECT * FROM pontos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Ponto?

    @Query("SELECT COUNT(*) FROM pontos")
    fun contar(): Flow<Int>

    @Insert
    suspend fun inserir(ponto: Ponto): Long

    @Update
    suspend fun atualizar(ponto: Ponto)

    @Delete
    suspend fun apagar(ponto: Ponto)

    @Query(
        "SELECT caminho_imagem FROM pontos " +
            "WHERE viagem_id = :viagemId AND caminho_imagem IS NOT NULL"
    )
    suspend fun caminhosDeImagemDaViagem(viagemId: Long): List<String>
}
