package com.mrgoodcat.hitmeup.domain.analitycs

import com.google.firebase.analytics.FirebaseAnalytics

interface HitMeUpFirebaseAnalytics {
    suspend fun getAnalytics(): FirebaseAnalytics
}