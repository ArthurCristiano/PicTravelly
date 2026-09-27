package com.app.pictravelly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.app.pictravelly.core.design.theme.PicTravellyTheme
import com.app.pictravelly.core.design.theme.ThemeController
import com.app.pictravelly.core.ui.PicTravellyAppScreen
import com.app.pictravelly.feature.settings.AppThemeSetting

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTheme by ThemeController.selectedTheme.collectAsState()
            val darkTheme = when (selectedTheme) {
                AppThemeSetting.LIGHT -> false
                AppThemeSetting.DARK -> true
                AppThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            PicTravellyTheme(darkTheme = darkTheme) {
                PicTravellyAppScreen()
            }
        }
    }
}