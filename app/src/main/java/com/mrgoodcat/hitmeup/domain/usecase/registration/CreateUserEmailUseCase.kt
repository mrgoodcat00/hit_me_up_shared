package com.mrgoodcat.hitmeup.domain.usecase.registration

import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.GLOBAL_ERROR_UNKNOWN_ERROR
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import com.mrgoodcat.hitmeup.presentation.registration.CreateUserResult
import timber.log.Timber
import javax.inject.Inject

class CreateUserEmailUseCase @Inject constructor(
    private val usersApi: FirebaseUsersApi,
    private val authorizationRepository: AuthorizationRepository,
) {
    suspend fun execute(email: String, password: String): CreateUserResult {
        Timber.d("invoke: ")
        try {
            val createdUserInFirebase = authorizationRepository.registerWithEmail(email, password)
            val createdUserInApi = usersApi.createUser(createdUserInFirebase)
            return CreateUserResult.Success(createdUserInApi)
        } catch (e: Exception) {
            return CreateUserResult.Error(e.message ?: GLOBAL_ERROR_UNKNOWN_ERROR)
        }
    }
}

