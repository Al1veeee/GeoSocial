package com.example.geosocial.routes

import com.example.geosocial.data.PlaceRepository
import com.example.geosocial.data.PostRepository
import com.example.geosocial.domain.AppException
import com.example.geosocial.dto.CreatePlaceRequest
import com.example.geosocial.dto.CreatePostRequest
import com.example.geosocial.dto.MessageResponse
import com.example.geosocial.plugins.AUTH_FIREBASE
import com.example.geosocial.security.requireUser
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.placeRoutes(
    placeRepository: PlaceRepository,
    postRepository: PostRepository
) {
    authenticate(AUTH_FIREBASE) {
        route("/places") {

            /**
             * GET /api/places?lat=55.75&lon=37.61&radius=5&query=кофе&category=cafe
             * Без координат — просто последние добавленные места.
             */
            get {
                val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
                val lon = call.request.queryParameters["lon"]?.toDoubleOrNull()
                val radius = call.request.queryParameters["radius"]?.toDoubleOrNull() ?: 10.0
                val query = call.request.queryParameters["query"]
                val category = call.request.queryParameters["category"]
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 50
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                call.respond(
                    placeRepository.search(lat, lon, radius, query, category, limit, offset)
                )
            }

            get("/{id}") {
                val id = call.longParam("id")
                val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
                val lon = call.request.queryParameters["lon"]?.toDoubleOrNull()
                call.respond(placeRepository.findById(id, lat, lon))
            }

            post {
                val user = call.requireUser()
                val body = call.receive<CreatePlaceRequest>()
                call.respond(HttpStatusCode.Created, placeRepository.create(user.userId, body))
            }

            delete("/{id}") {
                val user = call.requireUser()
                placeRepository.delete(call.longParam("id"), user.userId)
                call.respond(MessageResponse("Место удалено"))
            }

            /** Посты конкретного места. */
            get("/{id}/posts") {
                call.respond(postRepository.byPlace(call.longParam("id")))
            }

            post("/{id}/posts") {
                val user = call.requireUser()
                val body = call.receive<CreatePostRequest>()
                call.respond(
                    HttpStatusCode.Created,
                    postRepository.create(call.longParam("id"), user.userId, body)
                )
            }
        }
    }
}

internal fun io.ktor.server.application.ApplicationCall.longParam(name: String): Long =
    parameters[name]?.toLongOrNull() ?: throw AppException.BadRequest("Некорректный параметр $name")
