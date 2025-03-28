package com.mrgoodcat.hitmeup.domain.usecase.chats

import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatRemoteKeyModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import javax.inject.Inject

open class PrependUpdatedChatInListUseCase @Inject constructor(
    private val dbRepository: DbRepository,
) {
    suspend fun execute(chatModel: ChatLocalModel) {
        val dbRep = dbRepository.getDbInstance()
        val keysDao = dbRep.getChatsRemoteKeyDao()
        val chatsDao = dbRep.getChatsDao()

        dbRep.withTransaction {
            var chatKeyFromDb = keysDao.getChatKeyById(chatModel.chat_id)
            if (chatKeyFromDb != null && chatKeyFromDb.id.isNotEmpty()) {

                chatKeyFromDb = chatKeyFromDb.copy(createdAt = System.currentTimeMillis())
                keysDao.addNextChatKey(chatKeyFromDb)

                val updatedModel = if (chatsDao.getChat(chatModel.chat_id) != null) {
                    var unreadedCounter = chatsDao.getChat(chatModel.chat_id)?.unreadedCounter ?: 0
                    unreadedCounter++
                    chatModel.copy(unreadedCounter = unreadedCounter)
                } else {
                    chatModel
                }

                chatsDao.updateChat(updatedModel)
            } else {
                val lastCreatedChat = keysDao.getLastCreatedChat()
                if (lastCreatedChat != null && lastCreatedChat.id.isNotEmpty()) {
                    val chatKey = ChatRemoteKeyModel(
                        id = chatModel.chat_id,
                        previousChatId = lastCreatedChat.id,
                        nextChatId = chatModel.chat_id
                    )
                    keysDao.addNextChatKey(chatKey)
                } else {
                    val chatKey = ChatRemoteKeyModel(
                        id = chatModel.chat_id,
                        previousChatId = "",
                        nextChatId = chatModel.chat_id
                    )
                    keysDao.addNextChatKey(chatKey)
                }
                chatsDao.insertChat(chatModel)
            }
        }
    }
}