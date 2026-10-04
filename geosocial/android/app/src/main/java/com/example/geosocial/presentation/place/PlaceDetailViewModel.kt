package com.example.geosocial.presentation.place

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geosocial.domain.model.Place
import com.example.geosocial.domain.model.Post
import com.example.geosocial.domain.usecase.AddPostUseCase
import com.example.geosocial.domain.usecase.GetPlacePostsUseCase
import com.example.geosocial.domain.usecase.GetPlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaceDetailUiState(
    val place: Place? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val newPostText: String = "",
    val newPostRating: Int? = null,
    val isSending: Boolean = false
)

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlace: GetPlaceUseCase,
    private val getPlacePosts: GetPlacePostsUseCase,
    private val addPost: AddPostUseCase
) : ViewModel() {

    private val placeId: Long = savedStateHandle.get<Long>("placeId") ?: 0L

    private val _uiState = MutableStateFlow(PlaceDetailUiState())
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val placeResult = getPlace(placeId)
            val postsResult = getPlacePosts(placeId)
            _uiState.value = _uiState.value.copy(
                place = placeResult.getOrNull(),
                posts = postsResult.getOrDefault(emptyList()),
                isLoading = false,
                error = placeResult.exceptionOrNull()?.message
            )
        }
    }

    fun onPostTextChange(value: String) {
        _uiState.value = _uiState.value.copy(newPostText = value)
    }

    fun onRatingChange(value: Int?) {
        _uiState.value = _uiState.value.copy(newPostRating = value)
    }

    fun sendPost() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isSending = true, error = null)
            addPost(placeId, state.newPostText, state.newPostRating)
                .onSuccess { post ->
                    _uiState.value = _uiState.value.copy(
                        posts = listOf(post) + _uiState.value.posts,
                        newPostText = "",
                        newPostRating = null,
                        isSending = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isSending = false, error = it.message)
                }
        }
    }
}
