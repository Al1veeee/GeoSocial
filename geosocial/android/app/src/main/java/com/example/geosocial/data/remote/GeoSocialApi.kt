package com.example.geosocial.data.remote

import com.example.geosocial.data.remote.dto.*
import retrofit2.http.*

/** Описание REST API сервера. */
interface GeoSocialApi {

    @POST("api/auth/sync")
    suspend fun syncUser(): UserDto

    @GET("api/users/me")
    suspend fun getMe(): UserDto

    @PATCH("api/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserDto

    @GET("api/users/{id}/places")
    suspend fun getUserPlaces(@Path("id") userId: Long): List<PlaceDto>

    @GET("api/places")
    suspend fun getPlaces(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("radius") radius: Double? = null,
        @Query("query") query: String? = null,
        @Query("category") category: String? = null
    ): List<PlaceDto>

    @GET("api/places/{id}")
    suspend fun getPlace(@Path("id") id: Long): PlaceDto

    @POST("api/places")
    suspend fun createPlace(@Body body: CreatePlaceRequest): PlaceDto

    @DELETE("api/places/{id}")
    suspend fun deletePlace(@Path("id") id: Long): MessageResponse

    @GET("api/places/{id}/posts")
    suspend fun getPlacePosts(@Path("id") placeId: Long): List<PostDto>

    @POST("api/places/{id}/posts")
    suspend fun addPost(@Path("id") placeId: Long, @Body body: CreatePostRequest): PostDto

    @GET("api/feed")
    suspend fun getFeed(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): List<PostDto>
}
