package com.app.pictravelly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.database.model.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val userData: UserSettings) : MainActivityUiState
}

class MainActivityViewModel(
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _pendingSharedImages = MutableStateFlow<List<String>?>(null)
    val pendingSharedImages: StateFlow<List<String>?> = _pendingSharedImages.asStateFlow()

    val uiState: StateFlow<MainActivityUiState> = settingsRepository.userDataStream
        .map { MainActivityUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            initialValue = MainActivityUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun setPendingSharedImages(uris: List<String>) {
        _pendingSharedImages.value = uris
    }

    fun consumePendingSharedImages() {
        _pendingSharedImages.value = null
    }
}