package com.app.pictravelly.core.design.theme

import com.app.pictravelly.feature.settings.AppThemeSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeController {
    private val _selectedTheme = MutableStateFlow(AppThemeSetting.SYSTEM)
    val selectedTheme: StateFlow<AppThemeSetting> = _selectedTheme.asStateFlow()

    fun setTheme(theme: AppThemeSetting) {
        _selectedTheme.value = theme
    }
}