package com.app.pictravelly.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.pictravelly.MainActivityViewModel
import com.app.pictravelly.PicTravellyApp
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.core.map.MapConfigViewModel
import com.app.pictravelly.core.navigation.DestinationScreen
import com.app.pictravelly.feature.home.HomeViewModel
import com.app.pictravelly.feature.map.MapViewModel
import com.app.pictravelly.feature.settings.SettingsViewModel
import com.app.pictravelly.feature.spot_detail.SpotDetailViewModel
import com.app.pictravelly.feature.spot_form.SpotFormViewModel
import com.app.pictravelly.feature.spots.SpotsViewModel
import com.app.pictravelly.feature.trip_detail.TripDetailViewModel
import com.app.pictravelly.feature.trip_form.TripFormViewModel
import com.app.pictravelly.feature.trips.TripsViewModel

/**
 * Fábrica de ViewModels que provê instâncias com dependências manuais injetadas a partir do AppContainer.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                touristSpotRepository = picTravellyApplication().container.touristSpotRepository,
            )
        }

        initializer {
            SpotsViewModel(
                repository = picTravellyApplication().container.touristSpotRepository
            )
        }

        initializer {
            TripsViewModel(
                repository = picTravellyApplication().container.tripRepository
            )
        }

        initializer {
            MapViewModel(
                touristSpotRepository = picTravellyApplication().container.touristSpotRepository,
            )
        }

        initializer {
            SettingsViewModel(
                repository = picTravellyApplication().container.settingsRepository,
                touristSpotRepository = picTravellyApplication().container.touristSpotRepository
            )
        }

        initializer {
            MainActivityViewModel(
                settingsRepository = picTravellyApplication().container.settingsRepository
            )
        }

        initializer {
            MapConfigViewModel(
                settingsRepository = picTravellyApplication().container.settingsRepository
            )
        }
    }

    fun createSpotDetailFactory(
        spotId: Long,
        spotRepository: TouristSpotRepository,
        settingsRepository: SettingsRepository
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SpotDetailViewModel::class.java)) {
                    return SpotDetailViewModel(
                        touristSpotRepository = spotRepository,
                        settingsRepository = settingsRepository,
                        spotId = spotId
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    fun createTripDetailFactory(
        tripId: Long,
        tripRepository: TripRepository,
        spotRepository: TouristSpotRepository
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TripDetailViewModel::class.java)) {
                    return TripDetailViewModel(
                        tripRepository = tripRepository,
                        spotRepository = spotRepository,
                        tripId = tripId
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    fun createTripFormFactory(
        tripId: Long,
        tripRepository: TripRepository
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TripFormViewModel::class.java)) {
                    return TripFormViewModel(
                        repository = tripRepository,
                        tripId = tripId
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    fun createSpotFormFactory(
        tripId: Long = DestinationScreen.NO_TRIP_ID,
        spotId: Long = DestinationScreen.NO_SPOT_ID,
        spotRepository: TouristSpotRepository,
        tripRepository: TripRepository,
        settingsRepository: SettingsRepository
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SpotFormViewModel::class.java)) {
                    return SpotFormViewModel(
                        repository = spotRepository,
                        tripRepository = tripRepository,
                        settingsRepository = settingsRepository,
                        initialTripId = tripId,
                        spotId = spotId
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}

fun CreationExtras.picTravellyApplication(): PicTravellyApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PicTravellyApp)