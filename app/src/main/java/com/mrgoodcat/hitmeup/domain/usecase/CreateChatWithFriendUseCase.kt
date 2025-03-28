package com.mrgoodcat.hitmeup.domain.usecase

import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatRemoteKeyModel
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.FriendRemoteKeyModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toFriendLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import javax.inject.Inject

class CreateChatWithFriendUseCase @Inject constructor(
    private val chatsApi: FirebaseChatsApi,
    private val dbRepository: DbRepository,
    private val usersApi: FirebaseUsersApi,
) {
    suspend fun execute(collocutorId: String): ChatLocalModel? {
        val userProfile = dbRepository.getUserProfile() ?: return null
        val collocutorModel = usersApi.getUserById(collocutorId)
        val createdChat = chatsApi.createChatWithUser(userProfile.user_id, collocutorId).getOrNull()
            ?: return null

        if (collocutorModel.user_id.isEmpty() || collocutorModel.userDeleted) {
            return null
        }

        prependFriendInContactList(collocutorModel.toFriendLocalModel())
        prependUpdatedChatInList(createdChat)

        return createdChat
    }

    private suspend fun prependUpdatedChatInList(chatModel: ChatLocalModel) {
        val dbRep = dbRepository.getDbInstance()
        val keysDao = dbRep.getChatsRemoteKeyDao()
        val chatsDao = dbRep.getChatsDao()

        dbRep.withTransaction {
            var chatFromDb = keysDao.getChatKeyById(chatModel.chat_id)
            if (chatFromDb != null && chatFromDb.id.isNotEmpty()) {
                chatFromDb = chatFromDb.copy(createdAt = System.currentTimeMillis())
                keysDao.addNextChatKey(chatFromDb)

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

    private suspend fun prependFriendInContactList(newFriendModel: FriendLocalModel) {
        val dbRep = dbRepository.getDbInstance()
        val keysDao = dbRep.getFriendsRemoteKeyDao()
        val friendsDao = dbRep.getFriendDao()

        dbRep.withTransaction {
            val friendKey = keysDao.getFriendKeyById(newFriendModel.user_id)
            if (friendKey != null) {
                friendKey.createdAt = System.currentTimeMillis()
                keysDao.addNextFriendKey(friendKey)
                friendsDao.insertFriend(newFriendModel)
            } else {
                val lastCreatedFriend = keysDao.getLastCreatedFriend()
                if (lastCreatedFriend != null && lastCreatedFriend.id.isNotEmpty()) {
                    val friendNewKey = FriendRemoteKeyModel(
                        id = newFriendModel.user_id,
                        prevFriendId = lastCreatedFriend.id,
                        nextFriendId = newFriendModel.user_id,
                        createdAt = System.currentTimeMillis(),
                    )
                    keysDao.addNextFriendKey(friendNewKey)
                } else {
                    val friendNewKey = FriendRemoteKeyModel(
                        id = newFriendModel.user_id,
                        prevFriendId = "",
                        nextFriendId = newFriendModel.user_id,
                        createdAt = System.currentTimeMillis(),
                    )
                    keysDao.addNextFriendKey(friendNewKey)
                }
                friendsDao.insertFriend(newFriendModel)
            }
        }
    }
}