package com.example.geosocial.data.repository

import retrofit2.HttpException
import java.io.IOException

/** Единая обработка сетевых ошибок для всех репозиториев. */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    val message = when (e.code()) {
        401 -> "Требуется вход в приложение"
        403 -> "Нет прав на это действие"
        404 -> "Не найдено"
        else -> "Ошибка сервера (${e.code()})"
    }
    Result.failure(Exception(message))
} catch (e: IOException) {
    Result.failure(Exception("Нет связи с сервером. Проверьте, что сервер запущен."))
} catch (e: Exception) {
    Result.failure(Exception(e.message ?: "Неизвестная ошибка"))
}
