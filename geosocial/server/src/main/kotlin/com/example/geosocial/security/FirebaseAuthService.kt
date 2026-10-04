package com.example.geosocial.security

import com.example.geosocial.config.AppConfig
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

/** Данные пользователя, извлечённые из ID-токена Firebase. */
data class FirebaseUser(
    val uid: String,
    val email: String,
    val displayName: String
)

/**
 * Проверка Firebase ID-токена.
 *
 * Если путь к сервис-аккаунту не задан, а devMode = true, сервер принимает
 * «токены» вида `dev:<uid>` — это позволяет проверять API из Postman/curl
 * без настроенного Firebase.
 */
class FirebaseAuthService(private val config: AppConfig) {

    private val log = LoggerFactory.getLogger(FirebaseAuthService::class.java)
    private var initialized = false

    init {
        val path = config.firebaseServiceAccountPath
        if (path.isNotBlank() && File(path).exists()) {
            if (FirebaseApp.getApps().isEmpty()) {
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(FileInputStream(path)))
                    .build()
                FirebaseApp.initializeApp(options)
            }
            initialized = true
            log.info("Firebase Admin SDK инициализирован")
        } else {
            log.warn(
                "Firebase не настроен (firebase.serviceAccountPath пуст). " +
                        "Dev-режим: ${config.authDevMode}"
            )
        }
    }

    fun verify(token: String): FirebaseUser? {
        if (config.authDevMode && token.startsWith("dev:")) {
            val uid = token.removePrefix("dev:").trim()
            if (uid.isEmpty()) return null
            return FirebaseUser(uid = uid, email = "$uid@dev.local", displayName = uid)
        }
        if (!initialized) return null
        return runCatching {
            val decoded = FirebaseAuth.getInstance().verifyIdToken(token)
            FirebaseUser(
                uid = decoded.uid,
                email = decoded.email ?: "${decoded.uid}@unknown.local",
                displayName = decoded.name ?: decoded.email?.substringBefore("@") ?: decoded.uid
            )
        }.onFailure { log.debug("Не удалось проверить токен: ${it.message}") }.getOrNull()
    }
}
