package com.example.geosocial.routes

import com.example.geosocial.data.PlaceRepository
import com.example.geosocial.data.PostRepository
import com.example.geosocial.data.UserRepository
import com.example.geosocial.dto.MessageResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userRepository: UserRepository,
    placeRepository: PlaceRepository,
    postRepository: PostRepository
) {
    routing {
        get("/") {
            call.respond(MessageResponse("GeoSocial API работает. Смотри /api/health"))
        }
        route("/api") {
            get("/health") { call.respond(MessageResponse("ok")) }
            userRoutes(userRepository, placeRepository, postRepository)
            placeRoutes(placeRepository, postRepository)
            postRoutes(postRepository)
        }
    }
}
