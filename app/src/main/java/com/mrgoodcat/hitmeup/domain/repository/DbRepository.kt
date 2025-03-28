package com.mrgoodcat.hitmeup.domain.repository

import androidx.paging.PagingData
import com.mrgoodcat.hitmeup.data.db.HitMeUpDatabase
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel
import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel
import com.mrgoodcat.hitmeup.domain.model.AppSettingsModel
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import kotlinx.coroutines.flow.Flow

interface DbRepository {

    /*-------Users part -----*/

    suspend fun getUser(id: String): UserLocalModel?

    suspend fun insertUser(user: UserModel)

    suspend fun deleteUser(user: UserModel)

    suspend fun updateUser(user: UserModel)

    suspend fun clearUsers()

    suspend fun getCachedUser(userId: String): UserLocalModel?

    suspend fun getUserProfile(): UserProfileLocalModel?

    suspend fun insertUserProfile(user: UserProfileLocalModel)

    suspend fun clearProfile()

    /*-------Chats part -----*/

    suspend fun insertChat(chat: ChatModel)

    fun getChats(): Flow<PagingData<ChatLocalModel>>

    suspend fun getChat(id: String): ChatLocalModel?

    suspend fun updateChat(chat: ChatModel)

    suspend fun deleteChat(chat: ChatModel)

    suspend fun clearChats()

    suspend fun deleteChatKeysByChatId(chatId: String)

    /*-------Messages part -----*/

    suspend fun getMessages(chatId: String): Flow<PagingData<MessageLocalModel>>

    suspend fun getMessage(id: String): MessageLocalModel?

    suspend fun addMessages(messages: List<MessageModel>)

    suspend fun addMessage(message: MessageModel)

    suspend fun clearMessages()

    suspend fun deleteMessage(chatID: String)

    /*-------Messages remote keys part -----*/

    suspend fun deleteMessageKeys(chatID: String)

    /*-------Friends part -----*/

    suspend fun deleteFriendKeyById(friendId: String)

    suspend fun getFriend(friendId: String): FriendLocalModel?

    fun getFriends(): Flow<PagingData<FriendLocalModel>>

    suspend fun deleteFriend(friend: FriendLocalModel)

    suspend fun deleteFriend(friendId: String)

    suspend fun clearFriends()

    /*-------AppSettings part -----*/

    suspend fun insertAppSettings(settings: AppSettingsModel)

    suspend fun getAppSettings(): AppSettingsModel?

    suspend fun updateAppSettings(settings: AppSettingsModel)

    suspend fun clearAppSettings()


    fun getDbInstance(): HitMeUpDatabase
}