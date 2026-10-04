package com.example.geosocial.domain.repository

import com.example.geosocial.domain.model.Place
import com.example.geosocial.domain.model.Post
import com.example.geosocial.domain.model.User
import kotlinx.coroutines.flow.Flow

/** Аутентификация (Firebase) + синхронизация профиля с сервером. */
interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String, displayName: String): Result<Unit>
    fun signOut()
}

interface UserRepository {
    suspend fun getMe(): Result<User>
    suspend fun updateProfile(displayName: String?, bio: String?): Result<User>
    suspend fun getUserPlaces(userId: Long): Result<List<Place>>
}

interface PlaceRepository {
    suspend fun getPlaces(
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double = 10.0,
        query: String? = null,
        category: String? = null
    ): Result<List<Place>>

    suspend fun getPlace(id: Long): Result<Place>

    suspend fun createPlace(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double,
        address: String?
    ): Result<Place>

    suspend fun deletePlace(id: Long): Result<Unit>
}

interface PostRepository {
    suspend fun getFeed(limit: Int = 20, offset: Int = 0): Result<List<Post>>
    suspend fun getPlacePosts(placeId: Long): Result<List<Post>>
    suspend fun addPost(placeId: Long, text: String, rating: Int?): Result<Post>
}
