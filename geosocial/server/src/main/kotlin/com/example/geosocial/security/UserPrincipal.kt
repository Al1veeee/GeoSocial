package com.example.geosocial.security

import com.example.geosocial.domain.AppException
import io.ktor.server.application.*
import io.ktor.server.auth.*

/** Авторизованный пользователь текущего запроса. */
data class UserPrincipal(
    val userId: Long,
    val firebaseUid: String,
    val email: String,
    val displayName: String
)

fun ApplicationCall.requireUser(): UserPrincipal =
    principal<UserPrincipal>() ?: throw AppException.Unauthorized()
