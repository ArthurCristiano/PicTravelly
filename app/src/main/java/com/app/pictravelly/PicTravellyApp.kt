package com.app.pictravelly

import android.app.Application
import com.app.pictravelly.core.di.AppContainer
import com.app.pictravelly.core.di.DefaultAppContainer

/**
 * Application class onde inicializamos a Injeção de Dependência Manual via AppContainer.
 */
class PicTravellyApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}