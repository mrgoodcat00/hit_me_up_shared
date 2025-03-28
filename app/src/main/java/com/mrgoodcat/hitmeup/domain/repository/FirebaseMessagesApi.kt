package com.mrgoodcat.hitmeup.domain.repository

import com.mrgoodcat.hitmeup.domain.model.MessageModel
import kotlinx.coroutines.flow.Flow

interface FirebaseMessagesApi {
    suspend fun getMessagesAsList(
        chatId: String,
        pageSize: Int,
        lastItem: Long,
        direction: FirebaseMessageDirection
    ): List<MessageModel>

    suspend fun subscribeOnChatMessages(chatId: String): Flow<MessageModel>

    suspend fun sendMessage(message: MessageModel, currentChatId: String): Result<Boolean>
}

enum class FirebaseMessageDirection {
    ASC,
    DESC
}