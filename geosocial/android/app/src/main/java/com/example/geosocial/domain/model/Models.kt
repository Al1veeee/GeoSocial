package com.example.geosocial.domain.model

/** Пользователь социальной сети. */
data class User(
    val id: Long,
    val email: String,
    val displayName: String,
    val bio: String? = null,
    val avatarUrl: String? = null
)

/** Место на карте. */
data class Place(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val authorId: Long,
    val authorName: String,
    val postsCount: Long,
    val distanceKm: Double? = null,
    val createdAt: String
)

/** Пост (отзыв/чек-ин) в месте. */
data class Post(
    val id: Long,
    val placeId: Long,
    val placeTitle: String,
    val text: String,
    val photoUrl: String? = null,
    val rating: Int? = null,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String? = null,
    val createdAt: String
)

/** Категории мест. */
object PlaceCategories {
    val ALL = listOf("Кафе", "Парк", "Вид", "Спорт", "Культура", "Другое")
}
