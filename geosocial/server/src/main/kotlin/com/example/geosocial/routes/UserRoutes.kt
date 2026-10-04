package com.example.geosocial.routes

import com.example.geosocial.data.PlaceRepository
import com.example.geosocial.data.PostRepository
import com.example.geosocial.data.UserRepository
import com.example.geosocial.domain.AppException
import com.example.geosocial.dto.UpdateProfileRequest
import com.example.geosocial.plugins.AUTH_FIREBASE
import com.example.geosocial.security.requireUser
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(
    userRepository: UserRepository,
    placeRepository: PlaceRepository,
    postRepository: PostRepository
) {
    authenticate(AUTH_FIREBASE) {

        /** Регистрация/вход: профиль создаётся по Firebase-токену при первом обращении. */
        post("/auth/sync") {
            val user = call.requireUser()
            call.respond(
                userRepository.findById(user.userId) ?: throw AppException.NotFound("Пользователь")
            )
        }

        get("/users/me") {
            val user = call.requireUser()
            call.respond(
                userRepository.findById(user.userId) ?: throw AppException.NotFound("Пользователь")
            )
        }

        patch("/users/me") {
            val user = call.requireUser()
            val body = call.receive<UpdateProfileRequest>()
            call.respond(userRepository.updateProfile(user.userId, body))
        }

        get("/users/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: throw AppException.BadRequest("Некорректный id")
            call.respond(userRepository.findById(id) ?: throw AppException.NotFound("Пользователь"))
        }

        /** Места, созданные пользователем. */
        get("/users/{id}/places") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: throw AppException.BadRequest("Некорректный id")
            call.respond(placeRepository.findByAuthor(id))
        }

        /** Посты пользователя. */
        get("/users/{id}/posts") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: throw AppException.BadRequest("Некорректный id")
            call.respond(postRepository.byAuthor(id))
        }
    }
}
