package com.mrgoodcat.hitmeup.di

import android.content.Context
import com.mrgoodcat.hitmeup.data.repostory.AuthorizationRepositoryImpl
import com.mrgoodcat.hitmeup.data.repostory.FirebaseChatsApiImpl
import com.mrgoodcat.hitmeup.data.repostory.FirebaseMessagesApiImpl
import com.mrgoodcat.hitmeup.data.repostory.FirebaseUsersApiImpl
import com.mrgoodcat.hitmeup.data.repostory.MediaRepositoryImpl
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessagesApi
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import com.mrgoodcat.hitmeup.domain.repository.MediaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {
    @Singleton
    @Provides
    fun provideAuthorizationRepository(
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): AuthorizationRepository {
        return AuthorizationRepositoryImpl(dispatcher)
    }

    @Singleton
    @Provides
    fun provideMediaRepository(
        @ApplicationContext app: Context,
        @ApplicationScope appScope: CoroutineScope
    ): MediaRepository {
        return MediaRepositoryImpl(app, appScope)
    }

    @Provides
    @Singleton
    fun provideMessagesFirebaseRepository(
        @ApplicationContext app: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        @ApplicationScope appScope: CoroutineScope
    ): FirebaseMessagesApi {
        return FirebaseMessagesApiImpl(app, ioDispatcher, appScope)
    }

    @Provides
    @Singleton
    fun provideChatsFirebaseRepository(
        @ApplicationContext app: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): FirebaseChatsApi {
        return FirebaseChatsApiImpl(app, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideUserFirebaseRepository(
        @ApplicationContext app: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): FirebaseUsersApi {
        return FirebaseUsersApiImpl(app, ioDispatcher)
    }
}