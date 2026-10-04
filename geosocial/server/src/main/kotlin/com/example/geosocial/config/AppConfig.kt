package com.example.geosocial.config

import io.ktor.server.application.*

/** Настройки приложения, читаются из application.conf */
data class AppConfig(
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val firebaseServiceAccountPath: String,
    val authDevMode: Boolean
) {
    companion object {
        fun from(application: Application): AppConfig {
            val cfg = application.environment.config
            return AppConfig(
                dbUrl = cfg.property("db.url").getString(),
                dbUser = cfg.property("db.user").getString(),
                dbPassword = cfg.property("db.password").getString(),
                firebaseServiceAccountPath = cfg.property("firebase.serviceAccountPath").getString(),
                authDevMode = cfg.property("firebase.devMode").getString().toBoolean()
            )
        }
    }
}
