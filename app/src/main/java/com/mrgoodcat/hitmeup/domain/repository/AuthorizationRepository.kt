package com.mrgoodcat.hitmeup.domain.repository

import android.app.Activity
import androidx.activity.result.ActivityResultRegistryOwner
import com.google.firebase.auth.FirebaseUser
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import java.lang.ref.WeakReference

interface AuthorizationRepository {
    fun getAuthorization(): Boolean

    fun itsMyId(id: String): Boolean

    fun logOutFirebase()

    suspend fun registerWithEmail(email: String, password: String): FirebaseUser

    suspend fun authorizationWithGoogle(context: WeakReference<Activity>): AuthorizationResult

    suspend fun authorizationWithFacebook(context: WeakReference<ActivityResultRegistryOwner>): AuthorizationResult

    suspend fun authorizationWithEmail(login: String, password: String): AuthorizationResult
}