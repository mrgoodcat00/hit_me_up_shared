package com.mrgoodcat.hitmeup.domain.usecase.profile

import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

open class ClearUserDataUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dbRepository: DbRepository
) {
    suspend fun execute() {
        CoroutineScope(ioDispatcher).launch {
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
        }
    }
}