# GeoSocial — социальная сеть местоположений

Учебный клиент-серверный проект: пользователи добавляют места на карту, оставляют отзывы с оценками и смотрят ленту активности.

## Стек

- **Сервер:** Kotlin, Ktor 3, Exposed, PostgreSQL 16, Firebase Admin SDK
- **Клиент:** Android, Kotlin, Jetpack Compose, Retrofit, Hilt, Google Maps Compose, Firebase Authentication
- **Архитектура клиента:** Clean Architecture (presentation → domain ← data)

## Структура

- server/ REST API на Ktor + PostgreSQL
- android/ Android-приложение

## Запуск сервера

1. Поднять базу данных:
```bash
   cd server
   docker compose up -d
```
2. (Опционально) скачать JSON сервис-аккаунта в Firebase Console и указать путь:
```bash
   # Windows PowerShell
   $env:FIREBASE_SERVICE_ACCOUNT="C:\path\firebase-service-account.json"
```
   Без этого сервер работает в dev-режиме и принимает токен `Bearer dev:<любой_uid>`.
3. Запустить:
```bash
   gradle run
```
   Сервер доступен на `http://localhost:8080`, проверка — `GET /api/health`.

## Запуск клиента

1. В Firebase Console создать Android-приложение с package `com.example.geosocial`, включить **Authentication → Email/Password**, положить `google-services.json` в `android/app/`.
2. Получить ключ Google Maps SDK for Android и добавить в `android/local.properties`: MAPS_API_KEY=ваш_ключ
3. Открыть папку `android` в Android Studio и запустить на эмуляторе.

Адрес сервера задан в `app/build.gradle.kts` (`BASE_URL = http://10.0.2.2:8080/` — localhost хоста для эмулятора). Для реального устройства замените на IP компьютера в локальной сети.

## API

Все запросы, кроме `/api/health`, требуют заголовок `Authorization: Bearer <Firebase ID token>`.

| Метод | Путь | Описание |
|---|---|---|
| POST | `/api/auth/sync` | создать/получить профиль после входа |
| GET / PATCH | `/api/users/me` | свой профиль |
| GET | `/api/users/{id}/places` | места пользователя |
| GET | `/api/places?lat&lon&radius&query` | поиск мест (рядом / по названию) |
| POST | `/api/places` | создать место |
| GET | `/api/places/{id}` | место |
| DELETE | `/api/places/{id}` | удалить своё место |
| GET / POST | `/api/places/{id}/posts` | отзывы к месту |
| GET | `/api/feed` | общая лента |
| DELETE | `/api/posts/{id}` | удалить свой отзыв |

Пример в dev-режиме:
```bash
curl -X POST http://localhost:8080/api/places \
  -H "Authorization: Bearer dev:test1" -H "Content-Type: application/json" \
  -d '{"title":"Парк Горького","description":"Прогулки","category":"Парк","latitude":55.7298,"longitude":37.6014}'
```

## Схема БД

`users` → `places` (author_id) → `posts` (place_id, author_id). Таблицы создаются автоматически при старте сервера.
