package com.mrgoodcat.hitmeup.domain.usecase.chats

import android.content.Intent
import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatRemoteKeyModel
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_CHAT_ID_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_CONTENT_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_SENDER_EXTRA_KEY
import com.mrgoodcat.hitmeup.domain.model.FirebasePushMessageModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import javax.inject.Inject

open class OperatePushMessageInChatsListUseCase @Inject constructor(
    private val dbRepository: DbRepository,
    private val chatsApi: FirebaseChatsApi,
) {
    suspend fun execute(intent: Intent) {

        var pushMessage = FirebasePushMessageModel()
        var chatModel = ChatLocalModel()

        intent.extras?.keySet()?.map {
            when (it) {
                BROADCAST_CHAT_ID_EXTRA_KEY -> {
                    pushMessage = pushMessage.copy(chatId = intent.getStringExtra(it) ?: "")
                    chatModel = chatModel.copy(chat_id = intent.getStringExtra(it) ?: "")
                }

                BROADCAST_SENDER_EXTRA_KEY -> {
                    pushMessage = pushMessage.copy(senderId = intent.getStringExtra(it) ?: "")
                }

                BROADCAST_MESSAGE_CONTENT_EXTRA_KEY -> {
                    pushMessage = pushMessage.copy(messageText = intent.getStringExtra(it) ?: "")
                }

                BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY -> {
                    pushMessage =
                        pushMessage.copy(messageTimestamp = intent.getStringExtra(it) ?: "")
                }
            }
        }

        val dbRep = dbRepository.getDbInstance()
        val keysDao = dbRep.getChatsRemoteKeyDao()
        val chatsDao = dbRep.getChatsDao()

        dbRep.withTransaction {
            var chatKeyFromDb = keysDao.getChatKeyById(chatModel.chat_id)
            if (chatKeyFromDb != null && chatKeyFromDb.id.isNotEmpty()) {

                chatKeyFromDb = chatKeyFromDb.copy(createdAt = System.currentTimeMillis())
                keysDao.addNextChatKey(chatKeyFromDb)
                chatModel = chatsDao.getChat(chatModel.chat_id) ?: return@withTransaction

                chatModel = chatModel.copy(
                    last_message_timestamp = pushMessage.messageTimestamp.toLong(),
                    last_message_text = pushMessage.messageText,
                    last_message_sender = pushMessage.senderId
                )

                chatsDao.updateChat(chatModel)
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
                val chatFromApi = chatsApi.getChatById(chatModel.chat_id) ?: return@withTransaction
                chatsDao.insertChat(chatFromApi)
            }
        }
    }
}