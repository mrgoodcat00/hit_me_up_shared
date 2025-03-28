package com.mrgoodcat.hitmeup.domain.usecase

import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import javax.inject.Inject

class UpdateLastSeenUseCase @Inject constructor(
    private val firebaseUsersApi: FirebaseUsersApi,
) {
    suspend fun execute(userId: String) {
        firebaseUsersApi.updateLastSeenTime(userId)
    }
}