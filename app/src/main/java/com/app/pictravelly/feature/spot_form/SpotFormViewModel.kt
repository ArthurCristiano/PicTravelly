package com.app.pictravelly.feature.spot_form

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.database.model.touristSpot.SpotImageEntity
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotEntity
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.location.GeocodingHelper
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.navigation.DestinationScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class SpotFormTransientState(
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val latitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val longitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val imageUris: List<String> = emptyList(),
    val tripId: Long? = null,
    val isLocatingDevice: Boolean = false,
    val isResolvingAddress: Boolean = false,
    val isUsingFallbackLocation: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class SpotFormUiState(
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val latitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val longitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val imageUris: List<String> = emptyList(),
    val tripId: Long? = null,
    val availableTrips: List<TripEntity> = emptyList(),
    val isLocatingDevice: Boolean = false,
    val isResolvingAddress: Boolean = false,
    val isUsingFallbackLocation: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val mapEngine: MapEngineType = MapEngineType.OSM,
    val googleMapType: GoogleMapType = GoogleMapType.NORMAL
) {
    val isValid: Boolean get() = title.isNotBlank() && locationName.isNotBlank()
    val selectedTripTitle: String
        get() = availableTrips.firstOrNull { it.id == tripId }?.title ?: "Sem viagem"
}

class SpotFormViewModel(
    private val repository: TouristSpotRepository,
    tripRepository: TripRepository,
    settingsRepository: SettingsRepository,
    initialTripId: Long = DestinationScreen.NO_TRIP_ID
) : ViewModel() {

    private val _transientState = MutableStateFlow(
        SpotFormTransientState(tripId = initialTripId.takeIf { it != DestinationScreen.NO_TRIP_ID })
    )

    val uiState: StateFlow<SpotFormUiState> = combine(
        tripRepository.getTripsForPickerStream(),
        settingsRepository.userDataStream,
        _transientState
    ) { trips, settings, transient ->
        SpotFormUiState(
            title = transient.title,
            description = transient.description,
            locationName = transient.locationName,
            latitude = transient.latitude,
            longitude = transient.longitude,
            imageUris = transient.imageUris,
            tripId = transient.tripId,
            availableTrips = trips,
            isLocatingDevice = transient.isLocatingDevice,
            isResolvingAddress = transient.isResolvingAddress,
            isUsingFallbackLocation = transient.isUsingFallbackLocation,
            isSaving = transient.isSaving,
            errorMessage = transient.errorMessage,
            mapEngine = settings.mapEngine,
            googleMapType = settings.googleMapType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpotFormUiState()
    )

    fun updateTitle(newTitle: String) =
        _transientState.update { it.copy(title = newTitle, errorMessage = null) }

    fun updateDescription(newDescription: String) =
        _transientState.update { it.copy(description = newDescription) }

    fun updateLocationName(newLocationName: String) =
        _transientState.update { it.copy(locationName = newLocationName, errorMessage = null) }

    fun updateTripId(newTripId: Long?) = _transientState.update { it.copy(tripId = newTripId) }

    fun updateCoordinates(lat: Double, lng: Double, isFallback: Boolean = false) {
        _transientState.update {
            it.copy(
                latitude = lat,
                longitude = lng,
                isUsingFallbackLocation = isFallback,
                errorMessage = null
            )
        }
    }

    fun setLocatingDevice(isLocating: Boolean) =
        _transientState.update { it.copy(isLocatingDevice = isLocating) }

    fun setResolvingAddress(isResolving: Boolean) =
        _transientState.update { it.copy(isResolvingAddress = isResolving) }

    fun setErrorMessage(message: String?) =
        _transientState.update { it.copy(errorMessage = message) }

    fun onPickOnMap(context: Context, lat: Double, lng: Double) {
        updateCoordinates(lat, lng)
        resolveAddress(context, overwrite = true)
    }

    fun resolveAddress(context: Context, overwrite: Boolean = true) {
        val currentLat = _transientState.value.latitude
        val currentLng = _transientState.value.longitude
        if (!overwrite && _transientState.value.locationName.isNotBlank()) return

        viewModelScope.launch {
            setResolvingAddress(true)
            val address = GeocodingHelper.getAddressFromCoordinates(context.applicationContext, currentLat, currentLng)
            setResolvingAddress(false)

            if (address != null) {
                updateLocationName(address)
            } else if (overwrite) {
                setErrorMessage("Não foi possível obter o endereço. Você pode digitá-lo manualmente.")
            }
        }
    }

    fun captureLocation(context: Context, overwriteAddress: Boolean = false) {
        viewModelScope.launch {
            setLocatingDevice(true)
            val (lat, lng) = LocationHelper.getCurrentLocation(context.applicationContext)
            setLocatingDevice(false)

            val isFallback =
                lat == LocationHelper.DEFAULT_LATITUDE && lng == LocationHelper.DEFAULT_LONGITUDE
            updateCoordinates(lat, lng, isFallback = isFallback)

            if (isFallback) {
                if (overwriteAddress) setErrorMessage("Não foi possível obter a sua posição.")
                return@launch
            }
            resolveAddress(context, overwrite = overwriteAddress)
        }
    }

    fun onLocationPermissionDenied() {
        setErrorMessage("Sem permissão de localização. Toque no mapa para marcar o ponto.")
    }

    fun addImageUri(uri: String) {
        val current = _transientState.value.imageUris
        if (current.size < 5 && !current.contains(uri)) {
            _transientState.update { it.copy(imageUris = it.imageUris + uri) }
        }
    }

    fun addImageUris(uris: List<String>) {
        val current = _transientState.value.imageUris
        val availableSlots = (5 - current.size).coerceAtLeast(0)
        val toAdd = uris.filter { !current.contains(it) }.take(availableSlots)
        if (toAdd.isNotEmpty()) {
            _transientState.update { it.copy(imageUris = it.imageUris + toAdd) }
        }
    }

    fun removeImageUri(uri: String) =
        _transientState.update { it.copy(imageUris = it.imageUris - uri) }

    fun saveSpot(onSuccess: (Long) -> Unit) {
        val currentState = _transientState.value
        if (currentState.title.isBlank() || currentState.locationName.isBlank()) {
            setErrorMessage("Título e Localização são obrigatórios.")
            return
        }
        if (currentState.isSaving) return

        viewModelScope.launch {
            _transientState.update { it.copy(isSaving = true) }
            try {
                val spot = TouristSpotEntity(
                    tripId = currentState.tripId,
                    title = currentState.title.trim(),
                    description = currentState.description.trim(),
                    locationName = currentState.locationName.trim(),
                    visitDate = System.currentTimeMillis(),
                    latitude = currentState.latitude,
                    longitude = currentState.longitude
                )

                val images = currentState.imageUris.mapIndexed { index, uri ->
                    SpotImageEntity(spotId = 0, imageUri = uri, isCover = index == 0)
                }

                val generatedId = repository.insertSpotWithImages(spot, images)
                _transientState.update { it.copy(isSaving = false) }
                onSuccess(generatedId)
            } catch (e: Exception) {
                _transientState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Erro ao salvar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}