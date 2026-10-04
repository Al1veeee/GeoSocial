package com.example.geosocial.data.repository

import com.example.geosocial.data.mapper.toDomain
import com.example.geosocial.data.remote.GeoSocialApi
import com.example.geosocial.data.remote.dto.CreatePostRequest
import com.example.geosocial.domain.model.Post
import com.example.geosocial.domain.repository.PostRepository
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val api: GeoSocialApi
) : PostRepository {

    override suspend fun getFeed(limit: Int, offset: Int): Result<List<Post>> =
        safeApiCall { api.getFeed(limit, offset).map { it.toDomain() } }

    override suspend fun getPlacePosts(placeId: Long): Result<List<Post>> =
        safeApiCall { api.getPlacePosts(placeId).map { it.toDomain() } }

    override suspend fun addPost(placeId: Long, text: String, rating: Int?): Result<Post> =
        safeApiCall { api.addPost(placeId, CreatePostRequest(text, null, rating)).toDomain() }
}
