package com.app.pictravelly.core.banco

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.pictravelly.data.local.dao.PontoDao
import com.app.pictravelly.data.local.dao.ViagemDao
import com.app.pictravelly.data.local.entidade.Ponto
import com.app.pictravelly.data.local.entidade.Viagem

@Database(
    entities = [Viagem::class, Ponto::class],
    version = 1,
    exportSchema = false
)
abstract class BancoDeDados : RoomDatabase() {

    abstract fun viagemDao(): ViagemDao

    abstract fun pontoDao(): PontoDao

    companion object {
        @Volatile
        private var instancia: BancoDeDados? = null

        fun obter(context: Context): BancoDeDados =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    BancoDeDados::class.java,
                    "pictravelly.db"
                )
                    .build()
                    .also { instancia = it }
            }
    }
}
