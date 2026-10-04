package com.example.geosocial

import com.example.geosocial.config.AppConfig
import com.example.geosocial.data.PlaceRepository
import com.example.geosocial.data.PostRepository
import com.example.geosocial.data.UserRepository
import com.example.geosocial.db.DatabaseFactory
import com.example.geosocial.plugins.configureMonitoring
import com.example.geosocial.plugins.configureSecurity
import com.example.geosocial.plugins.configureSerialization
import com.example.geosocial.routes.configureRouting
import com.example.geosocial.security.FirebaseAuthService
import io.ktor.server.application.*

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val config = AppConfig.from(this)

    DatabaseFactory.init(config)

    val userRepository = UserRepository()
    val placeRepository = PlaceRepository()
    val postRepository = PostRepository()
    val authService = FirebaseAuthService(config)

    configureSerialization()
    configureMonitoring()
    configureSecurity(authService, userRepository)
    configureRouting(userRepository, placeRepository, postRepository)

    log.info("GeoSocial API запущен")
}
