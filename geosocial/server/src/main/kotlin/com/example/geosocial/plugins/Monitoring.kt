package com.example.geosocial.plugins

import com.example.geosocial.domain.AppException
import com.example.geosocial.dto.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) { level = Level.INFO }
    install(DefaultHeaders)
    install(CORS) {
        anyHost() // для учебного проекта
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }
    install(StatusPages) {
        exception<AppException> { call, cause ->
            val (status, code) = when (cause) {
                is AppException.NotFound -> HttpStatusCode.NotFound to "not_found"
                is AppException.BadRequest -> HttpStatusCode.BadRequest to "bad_request"
                is AppException.Unauthorized -> HttpStatusCode.Unauthorized to "unauthorized"
                is AppException.Forbidden -> HttpStatusCode.Forbidden to "forbidden"
            }
            call.respond(status, ErrorResponse(code, cause.message ?: "Ошибка"))
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Необработанная ошибка", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("internal_error", cause.message ?: "Внутренняя ошибка сервера")
            )
        }
    }
}
