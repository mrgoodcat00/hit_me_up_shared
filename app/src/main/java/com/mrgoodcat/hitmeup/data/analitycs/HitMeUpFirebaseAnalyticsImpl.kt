package com.mrgoodcat.hitmeup.data.analitycs

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import javax.inject.Inject

class HitMeUpFirebaseAnalyticsImpl @Inject constructor(
    val context: Context,
    val dbRepository: DbRepository,
) : HitMeUpFirebaseAnalytics {
    override suspend fun getAnalytics(): FirebaseAnalytics {
        val analytics = FirebaseAnalytics.getInstance(context)

        val userId = dbRepository.getUserProfile()?.user_id ?: "not_logged"
        analytics.setUserId(userId)

        return analytics
    }
}