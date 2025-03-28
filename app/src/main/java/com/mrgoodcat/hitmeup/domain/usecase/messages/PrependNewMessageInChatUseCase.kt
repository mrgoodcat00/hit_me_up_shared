package com.mrgoodcat.hitmeup.domain.usecase.messages

import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.model.MessageRemoteKeyModel
import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class PrependNewMessageInChatUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dbRepository: DbRepository,
) {
    suspend fun execute(message: MessageModel, chatId: String) {
        withContext(ioDispatcher) {
            val dbRep = dbRepository.getDbInstance()
            val keysDao = dbRep.getMessagesRemoteKeysDao()
            var messageToSave: MessageModel
            dbRep.withTransaction {
                val lastTimestamp = keysDao.getCreationTime(chatId) ?: System.currentTimeMillis()
                val newTimestamp = System.currentTimeMillis()
                messageToSave = message.copy(chat_id = chatId)

                val nextMessageKey = MessageRemoteKeyModel(
                    messageToSave.id,
                    chatId,
                    lastTimestamp,
                    newTimestamp,
                    newTimestamp
                )

                dbRep.getMessagesRemoteKeysDao().addNextMessageKey(nextMessageKey)
                dbRepository.addMessage(messageToSave)
            }
        }
    }
}