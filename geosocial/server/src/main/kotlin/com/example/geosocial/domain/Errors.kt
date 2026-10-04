package com.example.geosocial.domain

/** Ошибки уровня приложения, превращаются в HTTP-ответы в StatusPages. */
sealed class AppException(message: String) : RuntimeException(message) {
    class NotFound(what: String) : AppException("$what не найден")
    class BadRequest(message: String) : AppException(message)
    class Unauthorized(message: String = "Требуется авторизация") : AppException(message)
    class Forbidden(message: String = "Нет прав на это действие") : AppException(message)
}
