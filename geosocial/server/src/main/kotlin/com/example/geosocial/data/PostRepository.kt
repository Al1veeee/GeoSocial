package com.example.geosocial.data

import com.example.geosocial.db.DatabaseFactory.dbQuery
import com.example.geosocial.db.Places
import com.example.geosocial.db.Posts
import com.example.geosocial.db.Users
import com.example.geosocial.domain.AppException
import com.example.geosocial.dto.CreatePostRequest
import com.example.geosocial.dto.PostDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.deleteWhere
import java.time.LocalDateTime

class PostRepository {

    private val joined = Posts
        .innerJoin(Places, { Posts.placeId }, { Places.id })
        .innerJoin(Users, { Posts.authorId }, { Users.id })

    /** Общая лента: последние посты всех пользователей. */
    suspend fun feed(limit: Int, offset: Int): List<PostDto> = dbQuery {
        joined.selectAll()
            .orderBy(Posts.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map(::toDto)
    }

    suspend fun byPlace(placeId: Long): List<PostDto> = dbQuery {
        joined.selectAll().where { Posts.placeId eq placeId }
            .orderBy(Posts.createdAt to SortOrder.DESC)
            .map(::toDto)
    }

    suspend fun byAuthor(authorId: Long): List<PostDto> = dbQuery {
        joined.selectAll().where { Posts.authorId eq authorId }
            .orderBy(Posts.createdAt to SortOrder.DESC)
            .map(::toDto)
    }

    suspend fun create(placeId: Long, authorId: Long, request: CreatePostRequest): PostDto = dbQuery {
        if (request.text.isBlank()) throw AppException.BadRequest("Текст поста не может быть пустым")
        request.rating?.let {
            if (it !in 1..5) throw AppException.BadRequest("Оценка должна быть от 1 до 5")
        }
        Places.selectAll().where { Places.id eq placeId }.singleOrNull()
            ?: throw AppException.NotFound("Место")

        val newId = Posts.insertAndGetId {
            it[Posts.placeId] = placeId
            it[Posts.authorId] = authorId
            it[text] = request.text.trim()
            it[photoUrl] = request.photoUrl
            it[rating] = request.rating
            it[createdAt] = LocalDateTime.now()
        }
        joined.selectAll().where { Posts.id eq newId }.single().let(::toDto)
    }

    suspend fun delete(postId: Long, requesterId: Long) = dbQuery {
        val row = Posts.selectAll().where { Posts.id eq postId }.singleOrNull()
            ?: throw AppException.NotFound("Пост")
        if (row[Posts.authorId].value != requesterId) {
            throw AppException.Forbidden("Удалять пост может только его автор")
        }
        Posts.deleteWhere { Posts.id eq postId }
    }

    private fun toDto(row: ResultRow) = PostDto(
        id = row[Posts.id].value,
        placeId = row[Posts.placeId].value,
        placeTitle = row[Places.title],
        text = row[Posts.text],
        photoUrl = row[Posts.photoUrl],
        rating = row[Posts.rating],
        author = UserRepository.toShortDto(row),
        createdAt = row[Posts.createdAt].toString()
    )
}
