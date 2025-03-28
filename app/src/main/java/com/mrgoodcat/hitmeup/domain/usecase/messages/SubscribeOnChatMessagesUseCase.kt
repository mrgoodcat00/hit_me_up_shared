package com.mrgoodcat.hitmeup.domain.usecase.messages

import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessagesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class SubscribeOnChatMessagesUseCase @Inject constructor(
    private val firebaseMessagesApi: FirebaseMessagesApi
) {
    suspend fun execute(chatId: String): Flow<MessageModel> =
        firebaseMessagesApi.subscribeOnChatMessages(chatId)
}