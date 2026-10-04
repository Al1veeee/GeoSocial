package com.example.geosocial.domain.usecase

import com.example.geosocial.domain.model.Place
import com.example.geosocial.domain.model.Post
import com.example.geosocial.domain.model.User
import com.example.geosocial.domain.repository.AuthRepository
import com.example.geosocial.domain.repository.PlaceRepository
import com.example.geosocial.domain.repository.PostRepository
import com.example.geosocial.domain.repository.UserRepository
import javax.inject.Inject

// ---------- Аутентификация ----------

class SignInUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Заполните email и пароль"))
        }
        return repository.signIn(email.trim(), password)
    }
}

class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<Unit> {
        if (email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Пароль должен быть не короче 6 символов"))
        }
        return repository.signUp(email.trim(), password, name.trim())
    }
}

class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.signOut()
}

class ObserveAuthStateUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.isLoggedIn
}

// ---------- Места ----------

class GetPlacesUseCase @Inject constructor(private val repository: PlaceRepository) {
    suspend operator fun invoke(
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double = 10.0,
        query: String? = null,
        category: String? = null
    ): Result<List<Place>> = repository.getPlaces(latitude, longitude, radiusKm, query, category)
}

class GetPlaceUseCase @Inject constructor(private val repository: PlaceRepository) {
    suspend operator fun invoke(id: Long): Result<Place> = repository.getPlace(id)
}

class CreatePlaceUseCase @Inject constructor(private val repository: PlaceRepository) {
    suspend operator fun invoke(
        title: String,
        description: String,
        category: String,
        latitude: Double,
        longitude: Double,
        address: String?
    ): Result<Place> {
        if (title.isBlank()) return Result.failure(IllegalArgumentException("Введите название места"))
        if (description.isBlank()) return Result.failure(IllegalArgumentException("Введите описание"))
        return repository.createPlace(title, description, category, latitude, longitude, address)
    }
}

class DeletePlaceUseCase @Inject constructor(private val repository: PlaceRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = repository.deletePlace(id)
}

// ---------- Посты ----------

class GetFeedUseCase @Inject constructor(private val repository: PostRepository) {
    suspend operator fun invoke(): Result<List<Post>> = repository.getFeed()
}

class GetPlacePostsUseCase @Inject constructor(private val repository: PostRepository) {
    suspend operator fun invoke(placeId: Long): Result<List<Post>> = repository.getPlacePosts(placeId)
}

class AddPostUseCase @Inject constructor(private val repository: PostRepository) {
    suspend operator fun invoke(placeId: Long, text: String, rating: Int?): Result<Post> {
        if (text.isBlank()) return Result.failure(IllegalArgumentException("Введите текст поста"))
        return repository.addPost(placeId, text, rating)
    }
}

// ---------- Профиль ----------

class GetProfileUseCase @Inject constructor(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<User> = repository.getMe()
}

class UpdateProfileUseCase @Inject constructor(private val repository: UserRepository) {
    suspend operator fun invoke(displayName: String?, bio: String?): Result<User> =
        repository.updateProfile(displayName, bio)
}

class GetUserPlacesUseCase @Inject constructor(private val repository: UserRepository) {
    suspend operator fun invoke(userId: Long): Result<List<Place>> = repository.getUserPlaces(userId)
}
