package com.example.geosocial.data.mapper

import com.example.geosocial.data.remote.dto.PlaceDto
import com.example.geosocial.data.remote.dto.PostDto
import com.example.geosocial.data.remote.dto.UserDto
import com.example.geosocial.domain.model.Place
import com.example.geosocial.domain.model.Post
import com.example.geosocial.domain.model.User

fun UserDto.toDomain() = User(
    id = id,
    email = email,
    displayName = displayName,
    bio = bio,
    avatarUrl = avatarUrl
)

fun PlaceDto.toDomain() = Place(
    id = id,
    title = title,
    description = description,
    category = category,
    latitude = latitude,
    longitude = longitude,
    address = address,
    authorId = author.id,
    authorName = author.displayName,
    postsCount = postsCount,
    distanceKm = distanceKm,
    createdAt = createdAt
)

fun PostDto.toDomain() = Post(
    id = id,
    placeId = placeId,
    placeTitle = placeTitle,
    text = text,
    photoUrl = photoUrl,
    rating = rating,
    authorId = author.id,
    authorName = author.displayName,
    authorAvatar = author.avatarUrl,
    createdAt = createdAt
)
