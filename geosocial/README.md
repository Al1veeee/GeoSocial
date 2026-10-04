# GeoSocial — социальная сеть местоположений

Учебный клиент-серверный проект.

* **Сервер** — Kotlin + **Ktor** + **PostgreSQL** (Exposed), REST API.
* **Клиент** — Android, **Jetpack Compose**, **Retrofit**, **Clean Architecture**, DI на Hilt.
* **Авторизация** — **Firebase Authentication** (email + пароль) на клиенте; сервер проверяет
  Firebase ID-token через Firebase Admin SDK. Данные приложения хранятся **только в своей БД**,
  Firebase используется исключительно как провайдер аутентификации.

## Что умеет приложение

* Регистрация и вход по email/паролю.
* Карта с местами: маркеры, поиск, места «рядом со мной» (радиус в километрах).
* Добавление своего места: точка на карте, название, описание, категория, адрес.
* Посты (отзывы с оценкой 1–5) внутри места.
* Общая лента последних постов.
* Профиль: имя, «о себе», список своих мест; выход из аккаунта.

## Структура репозитория

```
server/    Ktor + PostgreSQL (REST API)
android/   Android-клиент (Compose + Clean Architecture)
```

---

## 1. Сервер

### Запуск БД

```bash
cd server
docker compose up -d        # поднимет PostgreSQL на localhost:5432
```

Либо создайте базу вручную:

```sql
CREATE DATABASE geosocial;
CREATE USER geosocial WITH PASSWORD 'geosocial';
GRANT ALL PRIVILEGES ON DATABASE geosocial TO geosocial;
```

Таблицы создаются автоматически при старте сервера (справочная схема — `server/src/main/resources/db/schema.sql`).

### Настройка Firebase на сервере

1. Firebase Console → Project settings → Service accounts → **Generate new private key**.
2. Сохраните файл, например, как `server/firebase-service-account.json`.
3. Запускайте сервер с переменной окружения:

```bash
# Windows PowerShell
$env:FIREBASE_SERVICE_ACCOUNT="C:\путь\firebase-service-account.json"
gradle run

# Linux / macOS
FIREBASE_SERVICE_ACCOUNT=./firebase-service-account.json ./gradlew run
```

Без этой переменной работает **dev-режим**: сервер принимает заголовок
`Authorization: Bearer dev:<любой_uid>` — удобно проверять API из Postman/curl,
не настраивая Firebase.

### Запуск

```bash
cd server
gradle run          # или ./gradlew run, если сгенерировать wrapper: gradle wrapper
```

Сервер поднимается на `http://localhost:8080`.

### Эндпоинты

Все запросы (кроме `/` и `/api/health`) требуют заголовок
`Authorization: Bearer <Firebase ID token>`.

| Метод  | Путь                      | Описание                                        |
|--------|---------------------------|-------------------------------------------------|
| GET    | `/api/health`             | проверка доступности                            |
| POST   | `/api/auth/sync`          | создать/получить профиль после входа в Firebase |
| GET    | `/api/users/me`           | свой профиль                                    |
| PATCH  | `/api/users/me`           | изменить имя / «о себе» / аватар                |
| GET    | `/api/users/{id}`         | профиль пользователя                            |
| GET    | `/api/users/{id}/places`  | места пользователя                              |
| GET    | `/api/users/{id}/posts`   | посты пользователя                              |
| GET    | `/api/places`             | список мест; параметры `lat`, `lon`, `radius`, `query`, `category`, `limit`, `offset` |
| GET    | `/api/places/{id}`        | одно место                                      |
| POST   | `/api/places`             | создать место                                   |
| DELETE | `/api/places/{id}`        | удалить своё место                              |
| GET    | `/api/places/{id}/posts`  | посты места                                     |
| POST   | `/api/places/{id}/posts`  | добавить пост в место                           |
| GET    | `/api/feed`               | общая лента постов                              |
| DELETE | `/api/posts/{id}`         | удалить свой пост                               |

Пример проверки в dev-режиме:

```bash
curl -X POST http://localhost:8080/api/auth/sync -H "Authorization: Bearer dev:test1"

curl -X POST http://localhost:8080/api/places \
  -H "Authorization: Bearer dev:test1" -H "Content-Type: application/json" \
  -d '{"title":"Парк Горького","description":"Хорошее место для прогулок","category":"Парк","latitude":55.7298,"longitude":37.6014}'

curl "http://localhost:8080/api/places?lat=55.75&lon=37.61&radius=10" -H "Authorization: Bearer dev:test1"
```

### Схема БД

```
users (id, firebase_uid, email, display_name, bio, avatar_url, created_at)
  │
  ├──< places (id, title, description, category, latitude, longitude, address, author_id, created_at)
  │        │
  └──────< posts (id, place_id, author_id, text, photo_url, rating, created_at)
```

---

## 2. Android-клиент

### Подготовка

1. **Firebase**: создайте проект, добавьте Android-приложение с package
   `com.example.geosocial`, включите **Authentication → Sign-in method → Email/Password**,
   скачайте `google-services.json` и положите его в `android/app/`.
2. **Google Maps**: получите ключ API (Maps SDK for Android) и добавьте в
   `android/local.properties`:

   ```properties
   MAPS_API_KEY=ваш_ключ
   ```

3. Откройте папку `android` в Android Studio и запустите на эмуляторе.

Базовый адрес сервера задан в `app/build.gradle.kts`:
`BASE_URL = "http://10.0.2.2:8080/"` — это localhost хост-машины со стороны эмулятора.
Для реального устройства подставьте IP компьютера в локальной сети.

### Архитектура клиента (Clean Architecture)

```
domain/        модели, интерфейсы репозиториев, use-case'ы  (не зависит ни от чего)
  model/       User, Place, Post
  repository/  AuthRepository, PlaceRepository, PostRepository, UserRepository
  usecase/     SignIn, SignUp, GetPlaces, CreatePlace, GetFeed, AddPost, ...

data/          реализация domain-интерфейсов
  remote/      GeoSocialApi (Retrofit), DTO, AuthInterceptor (подставляет Firebase-токен)
  auth/        FirebaseAuthDataSource
  mapper/      DTO → domain-модели
  repository/  *RepositoryImpl

presentation/  UI на Jetpack Compose (MVVM)
  auth/ feed/ map/ place/ createplace/ profile/ navigation/ components/

di/            Hilt-модули (сеть, репозитории)
```

Правило зависимостей: `presentation → domain ← data`. Слой `domain` не знает
ни про Retrofit, ни про Firebase, ни про Compose.

### Как это работает вместе

1. Пользователь входит через Firebase Auth (email + пароль) — клиент получает ID-token.
2. `AuthInterceptor` подставляет токен в каждый HTTP-запрос.
3. Ktor-сервер проверяет токен через Firebase Admin SDK и по `uid` находит/создаёт
   запись в своей таблице `users`.
4. Все данные (места, посты) лежат в PostgreSQL и отдаются через REST API.

---

## Возможные доработки

* Загрузка фотографий мест (multipart + хранение файлов на сервере).
* Лайки, комментарии, подписки на пользователей.
* Кэширование через Room для офлайн-режима.
* Пагинация ленты (Paging 3).
