package com.app.pictravelly

import android.app.Application
import com.app.pictravelly.core.di.ContainerDoApp

class PicTravellyApp : Application() {

    /** Grafo de dependencias do app, disponivel para todos os ViewModels. */
    lateinit var container: ContainerDoApp
        private set

    override fun onCreate() {
        super.onCreate()
        container = ContainerDoApp(this)
    }
}
