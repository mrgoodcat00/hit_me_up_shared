package com.mrgoodcat.hitmeup.domain.usecase.registration

import android.app.Activity
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import com.mrgoodcat.hitmeup.presentation.registration.CheckIsUserExists
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class CheckIsEmailAlreadyExistsGoogleUseCase @Inject constructor(
    private val authRepository: AuthorizationRepository,
    private val usersApi: FirebaseUsersApi
) {

    suspend fun execute(context: WeakReference<Activity>): CheckIsUserExists {
        Timber.d("invoke: ")
        when (val authResult = authRepository.authorizationWithGoogle(context)) {
            AuthorizationResult.ResultCanceled -> {
                Timber.d("ResultCanceled: ")
                return CheckIsUserExists.Error("ResultCanceled")
            }

            is AuthorizationResult.ResultFailed -> {
                Timber.d("ResultFailed:  " + authResult.message)
                return CheckIsUserExists.Error(authResult.message)
            }

            is AuthorizationResult.ResultFalse -> {
                Timber.d("ResultFalse: ")
                return CheckIsUserExists.Error("ResultFalse")
            }

            is AuthorizationResult.ResultSuccessful -> {
                Timber.d("ResultSuccessful: ")

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
                return CheckIsUserExists.Loading(false)
            }
        }
    }

}

