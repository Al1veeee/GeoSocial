package com.example.geosocial.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geosocial.domain.model.Place
import com.example.geosocial.domain.model.User
import com.example.geosocial.domain.usecase.GetProfileUseCase
import com.example.geosocial.domain.usecase.GetUserPlacesUseCase
import com.example.geosocial.domain.usecase.SignOutUseCase
import com.example.geosocial.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val places: List<Place> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editBio: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val getUserPlaces: GetUserPlacesUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            getProfile()
                .onSuccess { user ->
                    val places = getUserPlaces(user.id).getOrDefault(emptyList())
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        places = places,
                        isLoading = false,
                        editName = user.displayName,
                        editBio = user.bio.orEmpty()
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
        }
    }

    fun startEditing() { _uiState.value = _uiState.value.copy(isEditing = true) }
    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(editName = value) }
    fun onBioChange(value: String) { _uiState.value = _uiState.value.copy(editBio = value) }

    fun saveProfile() {
        val state = _uiState.value
        viewModelScope.launch {
            updateProfile(state.editName, state.editBio)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(user = it, isEditing = false)
                }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun logout() = signOut()
}
