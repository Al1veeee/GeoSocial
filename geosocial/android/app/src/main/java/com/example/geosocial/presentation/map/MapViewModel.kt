package com.example.geosocial.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geosocial.domain.model.Place
import com.example.geosocial.domain.usecase.GetPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val radiusKm: Double = 10.0,
    val query: String = ""
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPlaces: GetPlacesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init { load() }

    fun onUserLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(userLatitude = latitude, userLongitude = longitude)
        load()
    }

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
    }

    fun load() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            getPlaces(
                latitude = state.userLatitude,
                longitude = state.userLongitude,
                radiusKm = state.radiusKm,
                query = state.query.takeIf { it.isNotBlank() }
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(places = it, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
        }
    }
}
