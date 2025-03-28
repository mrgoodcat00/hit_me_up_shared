package com.mrgoodcat.hitmeup.domain.usecase.messages

import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessagesApi
import javax.inject.Inject

open class SendMessageUseCase @Inject constructor(
    private val firebaseMessagesApi: FirebaseMessagesApi
) {
    suspend fun execute(message: MessageModel, chatId: String): Result<Boolean> =
        firebaseMessagesApi.sendMessage(message, chatId)
}