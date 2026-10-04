package com.example.geosocial.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Подставляет в каждый запрос Firebase ID-token.
 * Сервер проверяет его через Firebase Admin SDK.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            firebaseAuth.currentUser?.let { user ->
                runCatching { user.getIdToken(false).await().token }.getOrNull()
            }
        }
        val request = chain.request().newBuilder()
            .apply { token?.let { header("Authorization", "Bearer $it") } }
            .build()
        return chain.proceed(request)
    }
}
