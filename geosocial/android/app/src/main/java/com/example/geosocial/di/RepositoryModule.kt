package com.example.geosocial.di

import com.example.geosocial.data.repository.AuthRepositoryImpl
import com.example.geosocial.data.repository.PlaceRepositoryImpl
import com.example.geosocial.data.repository.PostRepositoryImpl
import com.example.geosocial.data.repository.UserRepositoryImpl
import com.example.geosocial.domain.repository.AuthRepository
import com.example.geosocial.domain.repository.PlaceRepository
import com.example.geosocial.domain.repository.PostRepository
import com.example.geosocial.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: PlaceRepositoryImpl): PlaceRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}
