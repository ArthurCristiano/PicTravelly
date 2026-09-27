package com.app.pictravelly.core.database.model.settings

data class UserSettings(
    val theme: AppTheme,
    val language: AppLanguage,
    val mapEngine: MapEngineType
)