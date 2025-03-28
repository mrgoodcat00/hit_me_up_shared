package com.mrgoodcat.hitmeup.domain.usecase.registration

import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.GLOBAL_ERROR_ACTION_CANCELLED
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import com.mrgoodcat.hitmeup.presentation.registration.CheckIsUserExists
import timber.log.Timber
import javax.inject.Inject

class CheckIsEmailAlreadyExistsEmailUseCase @Inject constructor(
    private val authRepository: AuthorizationRepository,
    private val usersApi: FirebaseUsersApi
) {

    suspend fun execute(email: String, password: String): CheckIsUserExists {
        Timber.d("CheckIsEmailAlreadyExistsEmailUseCase invoke: ")
        when (val authResult = authRepository.authorizationWithEmail(email, password)) {
            AuthorizationResult.ResultCanceled -> {
                Timber.d("CheckIsEmailCheckIsEmail ResultCanceled: ")
                return CheckIsUserExists.Error(GLOBAL_ERROR_ACTION_CANCELLED)
            }

            is AuthorizationResult.ResultFailed -> {
                Timber.d("CheckIsEmailCheckIsEmail ResultFailed: ${authResult.message}")
                return CheckIsUserExists.Error(authResult.message)
            }

            is AuthorizationResult.ResultFalse -> {
                Timber.d("CheckIsEmailCheckIsEmail ResultFalse:")
                return CheckIsUserExists.Error(authResult.message)
            }

            is AuthorizationResult.ResultSuccessful -> {
                Timber.d("CheckIsEmail ResultSuccessful: ")

                if (authResult.user == null) {
                    return CheckIsUserExists.IsNotExists(null)
                }

                val apiUser = usersApi.getUserById(authResult.user.uid)

                if (apiUser.user_id.isEmpty()) {
                    return CheckIsUserExists.IsNotExists(authResult.user)
                }

                return CheckIsUserExists.IsExists(apiUser)
            }

            AuthorizationResult.IsLoading -> {
                Timber.d("CheckIsEmailCheckIsEmail IsLoading:false")
                return CheckIsUserExists.Loading(false)
            }
        }
    }
}

