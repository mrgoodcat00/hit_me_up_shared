package com.mrgoodcat.hitmeup.domain.usecase.registration

import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import javax.inject.Inject

class ReleaseAfterAuthorizationUseCase @Inject constructor(
    private val authRepository: AuthorizationRepository
) {
    fun execute() {
        authRepository.logOutFirebase()
    }
}

