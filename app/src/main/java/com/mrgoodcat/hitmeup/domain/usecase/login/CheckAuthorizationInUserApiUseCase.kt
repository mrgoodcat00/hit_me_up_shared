package com.mrgoodcat.hitmeup.domain.usecase.login

import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import javax.inject.Inject

class CheckAuthorizationInUserApiUseCase @Inject constructor(
    private val usersApi: FirebaseUsersApi
) {
    suspend fun execute(userId: String): Boolean {
        val userFromApi = usersApi.getUserById(userId)
        return userFromApi.user_id.isEmpty()
    }
}