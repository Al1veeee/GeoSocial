package com.example.geosocial.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val displayName: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String = ""
)

@Serializable
data class UserShortDto(
    val id: Long,
    val displayName: String,
    val avatarUrl: String? = null
)

@Serializable
data class PlaceDto(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val author: UserShortDto,
    val postsCount: Long = 0,
    val distanceKm: Double? = null,
    val createdAt: String = ""
)

@Serializable
data class PostDto(
    val id: Long,
    val placeId: Long,
    val placeTitle: String,
    val text: String,
    val photoUrl: String? = null,
    val rating: Int? = null,
    val author: UserShortDto,
    val createdAt: String = ""
)

@Serializable
data class CreatePlaceRequest(
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

@Serializable
data class CreatePostRequest(
    val text: String,
    val photoUrl: String? = null,
    val rating: Int? = null
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class MessageResponse(val message: String = "")

@Serializable
data class ErrorResponse(val error: String = "", val message: String = "")
