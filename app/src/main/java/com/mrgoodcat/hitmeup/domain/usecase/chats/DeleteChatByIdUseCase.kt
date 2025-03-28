package com.mrgoodcat.hitmeup.domain.usecase.chats

import com.mrgoodcat.hitmeup.data.repostory.UploadItemType
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import com.mrgoodcat.hitmeup.domain.repository.MediaRepository
import javax.inject.Inject

open class DeleteChatByIdUseCase @Inject constructor(
    private val chatsApi: FirebaseChatsApi,
    private val dbRepository: DbRepository,
    private val mediaRepository: MediaRepository,
) {
    suspend fun execute(chat: ChatModel) {
        val me = dbRepository.getUserProfile() ?: return

        mediaRepository.removeAllMediaFolderById(UploadItemType.Messages(chat.id))

        chatsApi.deleteChatById(chat, me.user_id)
    }
}