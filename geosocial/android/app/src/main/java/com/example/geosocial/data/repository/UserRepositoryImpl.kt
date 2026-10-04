package com.example.geosocial.data.repository

import com.example.geosocial.data.mapper.toDomain
import com.example.geosocial.data.remote.GeoSocialApi
import com.example.geosocial.data.remote.dto.UpdateProfileRequest
import com.example.geosocial.domain.model.Place
import com.example.geosocial.domain.model.User
import com.example.geosocial.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: GeoSocialApi
) : UserRepository {

    override suspend fun getMe(): Result<User> = safeApiCall { api.getMe().toDomain() }

    override suspend fun updateProfile(displayName: String?, bio: String?): Result<User> =
        safeApiCall { api.updateProfile(UpdateProfileRequest(displayName, bio)).toDomain() }

    override suspend fun getUserPlaces(userId: Long): Result<List<Place>> =
        safeApiCall { api.getUserPlaces(userId).map { it.toDomain() } }
}
