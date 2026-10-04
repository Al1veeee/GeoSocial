package com.example.geosocial.presentation.createplace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geosocial.domain.model.PlaceCategories
import com.example.geosocial.domain.usecase.CreatePlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePlaceUiState(
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val category: String = PlaceCategories.ALL.first(),
    val latitude: Double = 55.7558,
    val longitude: Double = 37.6173,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class CreatePlaceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createPlace: CreatePlaceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreatePlaceUiState(
            latitude = (savedStateHandle.get<Float>("lat") ?: 55.7558f).toDouble(),
            longitude = (savedStateHandle.get<Float>("lng") ?: 37.6173f).toDouble()
        )
    )
    val uiState: StateFlow<CreatePlaceUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) { _uiState.value = _uiState.value.copy(title = value) }
    fun onDescriptionChange(value: String) { _uiState.value = _uiState.value.copy(description = value) }
    fun onAddressChange(value: String) { _uiState.value = _uiState.value.copy(address = value) }
    fun onCategoryChange(value: String) { _uiState.value = _uiState.value.copy(category = value) }

    fun onLocationChange(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(latitude = latitude, longitude = longitude)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            createPlace(
                title = state.title,
                description = state.description,
                category = state.category,
                latitude = state.latitude,
                longitude = state.longitude,
                address = state.address.takeIf { it.isNotBlank() }
            )
                .onSuccess { _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isSaving = false, error = it.message)
                }
        }
    }
}
