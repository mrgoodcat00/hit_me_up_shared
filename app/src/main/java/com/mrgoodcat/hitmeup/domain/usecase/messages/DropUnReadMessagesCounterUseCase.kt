package com.mrgoodcat.hitmeup.domain.usecase.messages

import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.model.extensions.toChatModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class DropUnReadMessagesCounterUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dbRepository: DbRepository,
    private val chatsApi: FirebaseChatsApi,
) {
    suspend fun execute(chatId: String) {
        withContext(ioDispatcher) {
            var chatFromDb = dbRepository.getChat(chatId).toChatModel()

            if (chatFromDb == null) {
                chatFromDb = chatsApi.getChatById(chatId).toChatModel()
            }

            if (chatFromDb == null) {
                return@withContext
            }

            chatFromDb = chatFromDb.copy(unreadedCounter = 0)

            dbRepository.updateChat(chatFromDb)
        }
    }
}