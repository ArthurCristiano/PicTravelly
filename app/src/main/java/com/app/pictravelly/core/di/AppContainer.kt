package com.app.pictravelly.core.di

import android.content.Context
import com.app.pictravelly.core.data.OfflineTouristSpotRepository
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.PicTravellyDatabase

/**
 * Container de injeção de dependências manual da aplicação.
 */
interface AppContainer {
    val touristSpotRepository: TouristSpotRepository
}

/**
 * Implementação padrão do AppContainer com instâncias lazy/singleton.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val touristSpotRepository: TouristSpotRepository by lazy {
        OfflineTouristSpotRepository(
            PicTravellyDatabase.getDatabase(context).touristSpotDao()
        )
    }
}
