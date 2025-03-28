package com.mrgoodcat.hitmeup.domain.usecase.chats

import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatRemoteKeyModel
import com.mrgoodcat.hitmeup.data.repostory.ChatUpdateResult
import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.model.extensions.toAppSettingsModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toChatModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

open class SubscribeOnChatListChangesUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dbRepository: DbRepository,
    private val chatRepository: FirebaseChatsApi
) {
    suspend fun execute(): Boolean {
        val userProfile = dbRepository.getUserProfile() ?: return false

        chatRepository
            .subscribeOnChatUpdates(userProfile.user_id)
            .collect { firebaseEvent ->
                when (firebaseEvent) {
                    is ChatUpdateResult.OnAdded -> {
                        addChatIfIsNotExists(firebaseEvent.chat)
                        Timber.d("subscribeMessages: OnAdded ${firebaseEvent.chat}")
                    }

                    is ChatUpdateResult.OnChanged -> {
                        prependUpdatedChatInList(firebaseEvent.chat)
                        Timber.d("subscribeMessages: OnChanged ${firebaseEvent.chat}")
                    }

                    is ChatUpdateResult.OnMoved -> {
                        Timber.d("subscribeMessages: OnMoved ${firebaseEvent.chat}")
                    }

                    is ChatUpdateResult.OnRemoved -> {
                        removeDeletedChatAndMessages(firebaseEvent.chat)
                        Timber.d("subscribeMessages: OnRemoved ${firebaseEvent.chat}")
                    }
                }
            }
        return true
    }

    private fun addChatIfIsNotExists(chatModel: ChatLocalModel) {
        CoroutineScope(ioDispatcher).launch {
            val chat = dbRepository.getChat(chatModel.chat_id)
            if (chat == null) {
                prependUpdatedChatInList(chatModel)
            }
        }
    }

    private fun removeDeletedChatAndMessages(chatModel: ChatLocalModel) {
        val chat = chatModel.toChatModel() ?: return

        CoroutineScope(ioDispatcher).launch {
            dbRepository.getDbInstance().withTransaction {
                dbRepository.deleteChat(chat)
                dbRepository.deleteMessage(chat.id)
                dbRepository.deleteMessageKeys(chat.id)
                dbRepository.deleteChatKeysByChatId(chat.id)
            }
        }
    }

    private fun prependUpdatedChatInList(chatModel: ChatLocalModel) {
        CoroutineScope(ioDispatcher).launch {
            var chatModelToUpdate = chatModel.copy()
            val dbRep = dbRepository.getDbInstance()
            val keysDao = dbRep.getChatsRemoteKeyDao()
            val chatsDao = dbRep.getChatsDao()
            val appSettings = dbRep.getAppSettingsDao().getAppSettings().toAppSettingsModel()

            dbRep.withTransaction {
                var chatKeyFromDb = keysDao.getChatKeyById(chatModel.chat_id)
                if (chatKeyFromDb != null) {

                    chatKeyFromDb = chatKeyFromDb.copy(createdAt = System.currentTimeMillis())
                    keysDao.addNextChatKey(chatKeyFromDb)

                    val chatTemporary = chatsDao.getChat(chatModel.chat_id)
                    val chatOpenedId = appSettings?.currentOpenedChatId

                    val updatedModel = if (chatTemporary != null) {
                        var unreadedCount = chatTemporary.unreadedCounter

                        if (chatOpenedId.isNullOrEmpty() || chatOpenedId != chatModel.chat_id) {
                            unreadedCount++
                        }

                        chatModelToUpdate.copy(unreadedCounter = unreadedCount)
                    } else {
                        chatModelToUpdate
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
}