package com.example.geosocial.data.repository

import com.example.geosocial.data.mapper.toDomain
import com.example.geosocial.data.remote.GeoSocialApi
import com.example.geosocial.data.remote.dto.CreatePlaceRequest
import com.example.geosocial.domain.model.Place
import com.example.geosocial.domain.repository.PlaceRepository
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val api: GeoSocialApi
) : PlaceRepository {

    override suspend fun getPlaces(
        latitude: Double?,
        longitude: Double?,
        radiusKm: Double,
        query: String?,
        category: String?
    ): Result<List<Place>> = safeApiCall {
        api.getPlaces(
            lat = latitude,
            lon = longitude,
            radius = if (latitude != null) radiusKm else null,
            query = query?.takeIf { it.isNotBlank() },
            category = category
        ).map { it.toDomain() }
    }

    override suspend fun getPlace(id: Long): Result<Place> =
        safeApiCall { api.getPlace(id).toDomain() }

    override suspend fun createPlace(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double,
        address: String?
    ): Result<Place> = safeApiCall {
        api.createPlace(
            CreatePlaceRequest(title, description, category, latitude, longitude, address)
        ).toDomain()
    }

    override suspend fun deletePlace(id: Long): Result<Unit> = safeApiCall {
        api.deletePlace(id)
        Unit
    }
}
