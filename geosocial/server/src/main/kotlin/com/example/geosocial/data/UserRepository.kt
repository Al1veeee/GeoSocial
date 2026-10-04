package com.example.geosocial.data

import com.example.geosocial.db.DatabaseFactory.dbQuery
import com.example.geosocial.db.Users
import com.example.geosocial.domain.AppException
import com.example.geosocial.dto.UpdateProfileRequest
import com.example.geosocial.dto.UserDto
import com.example.geosocial.dto.UserShortDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class UserRepository {

    suspend fun findByFirebaseUid(uid: String): UserDto? = dbQuery {
        Users.selectAll().where { Users.firebaseUid eq uid }.singleOrNull()?.let(::toDto)
    }

    suspend fun findById(id: Long): UserDto? = dbQuery {
        Users.selectAll().where { Users.id eq id }.singleOrNull()?.let(::toDto)
    }

    /**
     * Создаёт профиль при первом входе или возвращает существующий.
     * Вызывается клиентом сразу после успешной авторизации в Firebase.
     */
    suspend fun syncUser(
        firebaseUid: String,
        email: String,
        displayName: String
    ): UserDto = dbQuery {
        val existing = Users.selectAll().where { Users.firebaseUid eq firebaseUid }.singleOrNull()
        if (existing != null) {
            toDto(existing)
        } else {
            val id = Users.insertAndGetId {
                it[Users.firebaseUid] = firebaseUid
                it[Users.email] = email
                it[Users.displayName] = displayName.ifBlank { email.substringBefore("@") }
                it[createdAt] = LocalDateTime.now()
            }
            Users.selectAll().where { Users.id eq id }.single().let(::toDto)
        }
    }

    suspend fun updateProfile(userId: Long, request: UpdateProfileRequest): UserDto = dbQuery {
        val updated = Users.update({ Users.id eq userId }) { row ->
            request.displayName?.let { row[displayName] = it }
            request.bio?.let { row[bio] = it }
            request.avatarUrl?.let { row[avatarUrl] = it }
        }
        if (updated == 0) throw AppException.NotFound("Пользователь")
        Users.selectAll().where { Users.id eq userId }.single().let(::toDto)
    }

    companion object {
        fun toDto(row: ResultRow) = UserDto(
            id = row[Users.id].value,
            email = row[Users.email],
            displayName = row[Users.displayName],
            bio = row[Users.bio],
            avatarUrl = row[Users.avatarUrl],
            createdAt = row[Users.createdAt].toString()
        )

        fun toShortDto(row: ResultRow) = UserShortDto(
            id = row[Users.id].value,
            displayName = row[Users.displayName],
            avatarUrl = row[Users.avatarUrl]
        )
    }
}
