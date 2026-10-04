package com.example.geosocial.data.repository

import com.example.geosocial.data.auth.FirebaseAuthDataSource
import com.example.geosocial.data.remote.GeoSocialApi
import com.example.geosocial.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebase: FirebaseAuthDataSource,
    private val api: GeoSocialApi
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = firebase.isLoggedIn

    override suspend fun signIn(email: String, password: String): Result<Unit> = try {
        firebase.signIn(email, password)
        // создаём/подтягиваем профиль на нашем сервере
        runCatching { api.syncUser() }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapFirebaseError(e)))
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<Unit> = try {
        firebase.signUp(email, password, displayName)
        runCatching { api.syncUser() }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mapFirebaseError(e)))
    }

    override fun signOut() = firebase.signOut()

    private fun mapFirebaseError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> "Пользователь с таким email не найден"
        is FirebaseAuthInvalidCredentialsException -> "Неверный email или пароль"
        is FirebaseAuthUserCollisionException -> "Такой email уже зарегистрирован"
        else -> e.message ?: "Ошибка авторизации"
    }
}
