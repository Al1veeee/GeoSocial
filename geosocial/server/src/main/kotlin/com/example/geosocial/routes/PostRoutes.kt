package com.example.geosocial.routes

import com.example.geosocial.data.PostRepository
import com.example.geosocial.dto.MessageResponse
import com.example.geosocial.plugins.AUTH_FIREBASE
import com.example.geosocial.security.requireUser
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.postRoutes(postRepository: PostRepository) {
    authenticate(AUTH_FIREBASE) {

        /** Общая лента постов. */
        get("/feed") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            call.respond(postRepository.feed(limit, offset))
        }

        delete("/posts/{id}") {
            val user = call.requireUser()
            postRepository.delete(call.longParam("id"), user.userId)
            call.respond(MessageResponse("Пост удалён"))
        }
    }
}
