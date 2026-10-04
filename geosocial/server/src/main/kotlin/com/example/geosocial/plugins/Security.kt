package com.example.geosocial.plugins

import com.example.geosocial.data.UserRepository
import com.example.geosocial.security.FirebaseAuthService
import com.example.geosocial.security.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*

const val AUTH_FIREBASE = "firebase"

fun Application.configureSecurity(
    authService: FirebaseAuthService,
    userRepository: UserRepository
) {
    install(Authentication) {
        bearer(AUTH_FIREBASE) {
            realm = "geosocial"
            authenticate { tokenCredential ->
                val firebaseUser = authService.verify(tokenCredential.token) ?: return@authenticate null
                // Профиль создаётся автоматически при первом запросе
                val user = userRepository.syncUser(
                    firebaseUid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName
                )
                UserPrincipal(
                    userId = user.id,
                    firebaseUid = firebaseUser.uid,
                    email = user.email,
                    displayName = user.displayName
                )
            }
        }
    }
}
