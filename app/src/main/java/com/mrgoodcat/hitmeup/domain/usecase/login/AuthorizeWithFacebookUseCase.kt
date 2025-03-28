package com.mrgoodcat.hitmeup.domain.usecase.login

import androidx.activity.result.ActivityResultRegistryOwner
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import java.lang.ref.WeakReference
import javax.inject.Inject

class AuthorizeWithFacebookUseCase @Inject constructor(private val authRepository: AuthorizationRepository) {

    suspend fun execute(context: WeakReference<ActivityResultRegistryOwner>): AuthorizationResult {
        return authRepository.authorizationWithFacebook(context)
    }

}