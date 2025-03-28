package com.mrgoodcat.hitmeup.di

import android.content.Context
import com.mrgoodcat.hitmeup.data.analitycs.HitMeUpFirebaseAnalyticsImpl
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object AnalyticsModule {

    @Provides
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context,
        dbRepository: DbRepository
    ): HitMeUpFirebaseAnalytics {
        return HitMeUpFirebaseAnalyticsImpl(context, dbRepository)
    }
}