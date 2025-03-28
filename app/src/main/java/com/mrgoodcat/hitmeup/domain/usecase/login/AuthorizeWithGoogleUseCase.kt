package com.mrgoodcat.hitmeup.domain.usecase.login

import android.app.Activity
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import java.lang.ref.WeakReference
import javax.inject.Inject

class AuthorizeWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthorizationRepository
) {
    suspend fun execute(context: WeakReference<Activity>): AuthorizationResult {
        return authRepository.authorizationWithGoogle(context)
    }
}