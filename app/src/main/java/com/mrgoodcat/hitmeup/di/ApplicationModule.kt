package com.mrgoodcat.hitmeup.di

import android.content.Context
import androidx.room.Room
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.db.HitMeUpDatabase
import com.mrgoodcat.hitmeup.data.db.TypeRoomConverter
import com.mrgoodcat.hitmeup.data.push_notification.PushNotificationBuilder
import com.mrgoodcat.hitmeup.data.repostory.DbRepositoryImpl
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.model.adapters.MoshiUriExplicitAdapter
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessagesApi
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import com.mrgoodcat.hitmeup.domain.utils.TextUtils
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    @Singleton
    fun providesHitMeUpDatabase(@ApplicationContext app: Context, moshi: Moshi): HitMeUpDatabase {
        val roomConverter = TypeRoomConverter(moshi)
        return Room
            .databaseBuilder(app, HitMeUpDatabase::class.java, app.getString(R.string.room_db_name))
            .addTypeConverter(roomConverter)
            .build()
    }

    @Provides
    @Singleton
    fun providesDbRepository(
        db: HitMeUpDatabase,
        messagesApi: FirebaseMessagesApi,
        chatsApi: FirebaseChatsApi,
        usersApi: FirebaseUsersApi,
        connectivityStateManager: ConnectivityStateManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DbRepository {
        return DbRepositoryImpl(
            db = db,
            messagesNetworkApi = messagesApi,
            chatsNetworkApi = chatsApi,
            usersNetworkApi = usersApi,
            connectivityManager = connectivityStateManager,
            ioDispatcher = ioDispatcher
        )
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext ctx: Context): ConnectivityStateManager {
        return ConnectivityStateManager(ctx)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().add(MoshiUriExplicitAdapter()).build()
    }

    @Provides
    @Singleton
    fun provideTextUtils(@ApplicationContext context: Context): TextUtils {
        return TextUtils(context)
    }

    @Provides
    @Singleton
    fun providePushNotificationBuilder(moshi: Moshi): PushNotificationBuilder {
        return PushNotificationBuilder(moshi)
    }
}
