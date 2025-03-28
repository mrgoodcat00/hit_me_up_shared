package com.mrgoodcat.hitmeup.presentation.home

import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import javax.inject.Inject

class CheckAuthorizationInUserApiUseCase @Inject constructor(
    private val usersApi: FirebaseUsersApi
) {
    suspend fun execute(userId: String): Boolean {
        return usersApi.getUserById(userId).user_id.isEmpty()
    }
}