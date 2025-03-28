package com.mrgoodcat.hitmeup.domain.usecase.profile

import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val authRepository: AuthorizationRepository) {

    fun execute() {
        authRepository.logOutFirebase()
    }

}