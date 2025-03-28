package com.mrgoodcat.hitmeup.domain.usecase.profile

import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val userRepository: FirebaseUsersApi,
    private val dbRepository: DbRepository,
    private val auth: AuthorizationRepository,
) {
    suspend fun execute(userId: String) {
        CoroutineScope(ioDispatcher).launch {
            val deleterRes = async { userRepository.deleteUserInDbApi(userId) }.await()

            Timber.d("1 update user in DB:$deleterRes")

            if (deleterRes) {
                launch {
                    userRepository.updateFcmToken(userId, null)
                    Timber.d("2 delete token")
                }.join()

                launch {
                    dbRepository.clearUsers()
                    dbRepository.clearMessages()
                    dbRepository.clearChats()
                    dbRepository.getFriends()
                    dbRepository.clearFriends()
                    dbRepository.clearProfile()
                    dbRepository.clearAppSettings()
                    dbRepository.getDbInstance().getChatsRemoteKeyDao().clearAllChatKeys()
                    dbRepository.getDbInstance().getMessagesRemoteKeysDao().clearAllMessageKeys()
                    dbRepository.getDbInstance().getFriendsRemoteKeyDao().clearAllFriendKeys()
                    Timber.d("3 delete token")
                }.join()

                launch {
                    val res = userRepository.deleteUserFirebaseAuth(userId)
                    Timber.d("4 delete user auth:$res")
                }.join()

                launch {
                    auth.logOutFirebase()
                    Timber.d("5 logout")
                }.join()
            }
        }
    }
}