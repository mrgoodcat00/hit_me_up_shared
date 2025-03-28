package com.mrgoodcat.hitmeup.domain.usecase.registration

import com.google.firebase.auth.FirebaseUser
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.GLOBAL_ERROR_UNKNOWN_ERROR
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import com.mrgoodcat.hitmeup.presentation.registration.CreateUserResult
import timber.log.Timber
import javax.inject.Inject

class CreateUserSocialUseCase @Inject constructor(
    private val usersApi: FirebaseUsersApi
) {
    suspend fun execute(user: FirebaseUser): CreateUserResult {
        Timber.d("invoke: ")
        try {
            val createUser = usersApi.createUser(user)
            return CreateUserResult.Success(createUser)
        } catch (e: Exception) {
            return CreateUserResult.Error(e.message ?: GLOBAL_ERROR_UNKNOWN_ERROR)
        }
    }
}

