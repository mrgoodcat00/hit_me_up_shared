package com.mrgoodcat.hitmeup.domain.usecase.login

import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import javax.inject.Inject

class AuthorizeWithEmailUseCase @Inject constructor(private val authRepository: AuthorizationRepository) {
    suspend fun execute(login: String, password: String): AuthorizationResult {
        return authRepository.authorizationWithEmail(login, password)
    }
}