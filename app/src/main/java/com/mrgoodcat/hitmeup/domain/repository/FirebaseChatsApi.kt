package com.mrgoodcat.hitmeup.domain.repository

import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.repostory.ChatUpdateResult
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import kotlinx.coroutines.flow.Flow

interface FirebaseChatsApi {
    suspend fun getChatsAsList(
        pageSize: Int,
        lastItem: String,
    ): List<ChatLocalModel>

    suspend fun getChatById(id: String): ChatLocalModel?

    suspend fun deleteChatById(chat: ChatModel, myId: String): Boolean

    suspend fun subscribeOnChatUpdates(myId: String): Flow<ChatUpdateResult>

    suspend fun createChatWithUser(ownerId: String, collocutorId: String): Result<ChatLocalModel>
}