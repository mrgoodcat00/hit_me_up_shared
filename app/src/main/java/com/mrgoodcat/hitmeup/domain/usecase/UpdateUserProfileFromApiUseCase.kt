package com.mrgoodcat.hitmeup.domain.usecase

import com.mrgoodcat.hitmeup.domain.model.extensions.toUserProfileLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import javax.inject.Inject

class UpdateUserProfileFromApiUseCase @Inject constructor(
    private val firebaseUsersApi: FirebaseUsersApi,
    private val dbRepository: DbRepository
) {
    suspend fun execute(id: String) {
        val currentUser = firebaseUsersApi.getUserById(id)
        dbRepository.insertUserProfile(currentUser.toUserProfileLocalModel())
    }
}