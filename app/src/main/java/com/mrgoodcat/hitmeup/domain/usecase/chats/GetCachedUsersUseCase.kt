package com.mrgoodcat.hitmeup.domain.usecase.chats

import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import javax.inject.Inject

open class GetCachedUsersUseCase @Inject constructor(
    private val dbRepository: DbRepository,
    private val usersApi: FirebaseUsersApi,
) {
    suspend fun execute(userId: List<String>): List<UserModel> {
        val resultArray: MutableList<UserModel> = ArrayList()
        userId.map { id ->
            val user = dbRepository.getCachedUser(id)
            if (user == null) {
                val userFromApi = usersApi.getUserById(id)
                dbRepository.insertUser(userFromApi.toUserModel())
                resultArray.add(userFromApi.toUserModel())
            } else {
                resultArray.add(user.toUserModel())
            }
        }
        return resultArray
    }
}