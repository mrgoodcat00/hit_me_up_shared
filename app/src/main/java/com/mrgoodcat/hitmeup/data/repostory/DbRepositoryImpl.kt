package com.mrgoodcat.hitmeup.data.repostory

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.db.HitMeUpDatabase
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel
import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel
import com.mrgoodcat.hitmeup.data.pagination.ChatsRemoteMediator
import com.mrgoodcat.hitmeup.data.pagination.FriendsRemoteMediator
import com.mrgoodcat.hitmeup.data.pagination.MessagesRemoteMediator
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CHATS_PAGE_SIZE
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CONTACTS_PAGE_SIZE
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.MESSAGES_PAGE_SIZE
import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.model.AppSettingsModel
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toAppSettingsLocalModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toAppSettingsModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toChatLocalModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toMessagesLocalModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessagesApi
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DbRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val db: HitMeUpDatabase,
    private val messagesNetworkApi: FirebaseMessagesApi,
    private val chatsNetworkApi: FirebaseChatsApi,
    private val usersNetworkApi: FirebaseUsersApi,
    private val connectivityManager: ConnectivityStateManager,
) : DbRepository {

    override suspend fun getUser(id: String): UserLocalModel? {
        return withContext(ioDispatcher) {
            db.getUserDao().getUser(id)
        }
    }

    override suspend fun insertUser(user: UserModel) {
        withContext(ioDispatcher) {
            db.getUserDao().insertUser(user.toUserLocalModel())
        }
    }

    override suspend fun deleteUser(user: UserModel) {
        withContext(ioDispatcher) {
            db.getUserDao().deleteUser(user.toUserLocalModel())
        }
    }

    override suspend fun updateUser(user: UserModel) {
        withContext(ioDispatcher) {
            db.getUserDao().updateUser(user.toUserLocalModel())
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getFriends(): Flow<PagingData<FriendLocalModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = CONTACTS_PAGE_SIZE,
                prefetchDistance = 10,
                enablePlaceholders = false,
            ),
            0,
            pagingSourceFactory = { db.getFriendDao().getFriends() },
            remoteMediator = FriendsRemoteMediator(db, usersNetworkApi, connectivityManager)
        ).flow
    }

    override suspend fun deleteFriend(friend: FriendLocalModel) {
        withContext(ioDispatcher) {
            db.getFriendDao().deleteFriend(friend)
        }
    }

    override suspend fun deleteFriend(friendId: String) {
        withContext(ioDispatcher) {
            db.getFriendDao().deleteFriend(friendId)
        }
    }

    override suspend fun clearFriends() {
        withContext(ioDispatcher) {
            db.getFriendDao().clearDb()
        }
    }

    override suspend fun insertAppSettings(settings: AppSettingsModel) {
        withContext(ioDispatcher) {
            settings.toAppSettingsLocalModel()?.let {
                db.getAppSettingsDao().insertAppSettings(it)
            }
        }
    }

    override suspend fun getAppSettings(): AppSettingsModel? {
        return withContext(ioDispatcher) {
            db.getAppSettingsDao().getAppSettings().toAppSettingsModel()
        }
    }

    override suspend fun updateAppSettings(settings: AppSettingsModel) {
        withContext(ioDispatcher) {
            settings.toAppSettingsLocalModel()?.let {
                db.getAppSettingsDao().updateAppSettings(it)
            }
        }
    }

    override suspend fun clearAppSettings() {
        withContext(ioDispatcher) {
            db.getAppSettingsDao().clearDb()
        }
    }

    override suspend fun clearUsers() {
        withContext(ioDispatcher) {
            db.getUserDao().clearDb()
        }
    }

    override suspend fun getCachedUser(userId: String): UserLocalModel {
        return withContext(ioDispatcher) {
            db.withTransaction {
                var user = db.getUserDao().getUser(userId)
                if (user == null) {
                    val userFromApi = usersNetworkApi.getUserById(userId)
                    db.getUserDao().insertUser(userFromApi)
                    user = userFromApi
                }
                user
            }
        }
    }

    override suspend fun getUserProfile(): UserProfileLocalModel? {
        return withContext(ioDispatcher) {
            db.getProfileDao().getMe()
        }
    }

    override suspend fun insertUserProfile(user: UserProfileLocalModel) {
        return withContext(ioDispatcher) {
            db.getProfileDao().insertMe(user)
        }
    }

    override suspend fun clearProfile() {
        CoroutineScope(ioDispatcher).launch {
            db.getProfileDao().clearDb()
        }
    }

    override suspend fun insertChat(chat: ChatModel) {
        withContext(ioDispatcher) {
            db.getChatsDao().insertChat(chat.toChatLocalModel())
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getChats(): Flow<PagingData<ChatLocalModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = CHATS_PAGE_SIZE,
                prefetchDistance = 10,
                enablePlaceholders = false,
            ),
            0,
            pagingSourceFactory = { db.getChatsDao().getChats() },
            remoteMediator = ChatsRemoteMediator(db, chatsNetworkApi, connectivityManager)
        ).flow
    }

    override suspend fun getChat(id: String): ChatLocalModel? {
        return withContext(ioDispatcher) {
            db.getChatsDao().getChat(id)
        }
    }

    override suspend fun updateChat(chat: ChatModel) {
        withContext(ioDispatcher) {
            db.getChatsDao().updateChat(chat.toChatLocalModel())
        }
    }

    override suspend fun deleteChat(chat: ChatModel) {
        withContext(ioDispatcher) {
            db.getChatsDao().deleteChat(chat.toChatLocalModel())
        }
    }

    override suspend fun clearChats() {
        withContext(ioDispatcher) {
            db.getChatsDao().clearDb()
        }
    }

    override suspend fun deleteChatKeysByChatId(chatId: String) {
        withContext(ioDispatcher) {
            db.getChatsRemoteKeyDao().clearAllChatKeysByChatId(chatId)
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override suspend fun getMessages(chatId: String): Flow<PagingData<MessageLocalModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = MESSAGES_PAGE_SIZE,
                prefetchDistance = 30,
                enablePlaceholders = false,
            ),
            initialKey = 0,
            pagingSourceFactory = { db.getMessageDao().getMessages(chatId = chatId) },
            remoteMediator = MessagesRemoteMediator(
                chatId,
                db,
                messagesNetworkApi,
                connectivityManager
            )
        ).flow
    }

    override suspend fun getMessage(id: String): MessageLocalModel? {
        return withContext(ioDispatcher) {
            db.getMessageDao().getMessage(id)
        }
    }

    override suspend fun addMessages(messages: List<MessageModel>) {
        withContext(ioDispatcher) {
            db.getMessageDao().insertMessages(messages.map { it.toMessagesLocalModel() })
        }
    }

    override suspend fun addMessage(message: MessageModel) {
        withContext(ioDispatcher) {
            db.getMessageDao().insertMessage(message.toMessagesLocalModel())
        }
    }

    override suspend fun clearMessages() {
        withContext(ioDispatcher) {
            db.getMessageDao().clearMessagesFromDb()
        }
    }

    override suspend fun deleteMessage(chatID: String) {
        withContext(ioDispatcher) {
            db.getMessageDao().deleteMessage(chatID)
        }
    }

    override suspend fun deleteMessageKeys(chatID: String) {
        withContext(ioDispatcher) {
            db.getMessagesRemoteKeysDao().deleteMessageKeysByChatId(chatID)
        }
    }

    override suspend fun deleteFriendKeyById(friendId: String) {
        withContext(ioDispatcher) {
            db.getFriendsRemoteKeyDao().deleteFriendKeysById(friendId)
        }
    }

    override suspend fun getFriend(friendId: String): FriendLocalModel? {
        return withContext(ioDispatcher) {
            db.getFriendDao().getFriend(friendId)
        }
    }

    override fun getDbInstance(): HitMeUpDatabase {
        return db
    }

}