package com.app.pictravelly.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.pictravelly.data.local.entidade.Viagem
import com.app.pictravelly.data.local.relacao.ViagemComPontos
import kotlinx.coroutines.flow.Flow

@Dao
interface ViagemDao {

    @Transaction
    @Query("SELECT * FROM viagens ORDER BY data_inicio DESC")
    fun observarTodasComPontos(): Flow<List<ViagemComPontos>>

    @Transaction
    @Query("SELECT * FROM viagens WHERE id = :id")
    fun observarComPontos(id: Long): Flow<ViagemComPontos?>

    @Query("SELECT * FROM viagens ORDER BY data_inicio DESC")
    fun observarTodas(): Flow<List<Viagem>>

    @Query("SELECT * FROM viagens WHERE id = :id")
    suspend fun buscarPorId(id: Long): Viagem?

    @Query("SELECT COUNT(*) FROM viagens")
    fun contar(): Flow<Int>

    @Insert
    suspend fun inserir(viagem: Viagem): Long

    @Update
    suspend fun atualizar(viagem: Viagem)

    @Delete
    suspend fun apagar(viagem: Viagem)
}
