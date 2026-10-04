package com.example.geosocial.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

/** Пользователи. firebaseUid — идентификатор из Firebase Authentication. */
object Users : LongIdTable("users") {
    val firebaseUid = varchar("firebase_uid", 128).uniqueIndex()
    val email = varchar("email", 255)
    val displayName = varchar("display_name", 100)
    val bio = varchar("bio", 500).nullable()
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val createdAt = datetime("created_at")
}

/** Места на карте — основная сущность «социальной сети местоположений». */
object Places : LongIdTable("places") {
    val title = varchar("title", 150)
    val description = varchar("description", 1000)
    val category = varchar("category", 50)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val address = varchar("address", 300).nullable()
    val authorId = reference("author_id", Users, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at")
}

/** Посты (отзывы/чек-ины) пользователей в конкретном месте. */
object Posts : LongIdTable("posts") {
    val placeId = reference("place_id", Places, onDelete = ReferenceOption.CASCADE)
    val authorId = reference("author_id", Users, onDelete = ReferenceOption.CASCADE)
    val text = varchar("text", 2000)
    val photoUrl = varchar("photo_url", 500).nullable()
    val rating = integer("rating").nullable()
    val createdAt = datetime("created_at")
}
