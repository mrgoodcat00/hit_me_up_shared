package com.mrgoodcat.hitmeup.domain.usecase.messages

import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toChatModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import javax.inject.Inject

class GetChatUseCase @Inject constructor(
    private val dbRepository: DbRepository,
    private val firebaseChatsApi: FirebaseChatsApi
) {
    suspend fun execute(chatId: String): ChatModel? =
        dbRepository.getChat(chatId).toChatModel() ?: firebaseChatsApi.getChatById(chatId)
            .toChatModel()
}