package com.app.pictravelly

import android.app.Application
import android.preference.PreferenceManager
import com.app.pictravelly.core.di.AppContainer
import com.app.pictravelly.core.di.DefaultAppContainer
import org.osmdroid.config.Configuration

/**
 * Application class onde inicializamos a Injeção de Dependência Manual via AppContainer.
 */
class PicTravellyApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        runCatching {
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
            Configuration.getInstance().userAgentValue = packageName
        }
    }
}