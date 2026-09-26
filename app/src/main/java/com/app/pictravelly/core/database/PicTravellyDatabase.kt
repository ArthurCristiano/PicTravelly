package com.app.pictravelly.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.pictravelly.core.database.dao.TouristSpotDao
import com.app.pictravelly.core.database.dao.TripDao
import com.app.pictravelly.core.database.model.SpotImageEntity
import com.app.pictravelly.core.database.model.TouristSpotEntity
import com.app.pictravelly.core.database.model.TripEntity

/**
 * Banco de dados principal Room da aplicação.
 */
@Database(
    entities = [TripEntity::class, TouristSpotEntity::class, SpotImageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PicTravellyDatabase : RoomDatabase() {

    abstract fun touristSpotDao(): TouristSpotDao

    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var Instance: PicTravellyDatabase? = null

        fun getDatabase(context: Context): PicTravellyDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PicTravellyDatabase::class.java,
                    "pictravelly_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}