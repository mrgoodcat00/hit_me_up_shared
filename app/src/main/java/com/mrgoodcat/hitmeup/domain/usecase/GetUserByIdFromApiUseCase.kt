package com.mrgoodcat.hitmeup.domain.usecase

import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import javax.inject.Inject

open class GetUserByIdFromApiUseCase @Inject constructor(
    private val firebaseUsersApi: FirebaseUsersApi,
    private val dbRepository: DbRepository
) {
    suspend fun execute(userId: String, withUpdate: Boolean = false): UserLocalModel {
        val user = dbRepository.getUser(userId)

        if (user == null || withUpdate) {
            val userFromApi = firebaseUsersApi.getUserById(userId)
            dbRepository.insertUser(userFromApi.toUserModel())

            return userFromApi
        }

        return user
    }
}