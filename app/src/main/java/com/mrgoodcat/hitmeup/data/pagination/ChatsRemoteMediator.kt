package com.mrgoodcat.hitmeup.data.pagination

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.db.HitMeUpDatabase
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatRemoteKeyModel
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CHATS_PAGE_SIZE
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import timber.log.Timber
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class ChatsRemoteMediator(
    private val dataBase: HitMeUpDatabase,
    private val firebaseChats: FirebaseChatsApi,
    private val connectivityManager: ConnectivityStateManager
) : RemoteMediator<Int, ChatLocalModel>() {

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)
        val lastKeyCreated = dataBase.getChatsRemoteKeyDao().getCreationTime() ?: 0L

        if (!connectivityManager.isOnline()) {
            return InitializeAction.SKIP_INITIAL_REFRESH
        }

        if (lastKeyCreated == 0L) {
            return InitializeAction.LAUNCH_INITIAL_REFRESH
        }

        return if (System.currentTimeMillis() - lastKeyCreated >= cacheTimeout) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ChatLocalModel>
    ): MediatorResult {
        return try {
            Timber.d("ChatsRemoteMediator loadType:$loadType")

            val lastChatId = when (loadType) {
                LoadType.REFRESH -> null

                LoadType.PREPEND -> {
                    val firstItem = getRemoteKeyForFirstItem(state)
                    val prevChatId = firstItem?.previousChatId

                    prevChatId ?: return MediatorResult.Success(
                        endOfPaginationReached = prevChatId != null
                    )
                }

                LoadType.APPEND -> {
                    val lastItem = getRemoteKeyForLastItem(state)
                    val nextChatId = lastItem?.nextChatId

                    nextChatId ?: return MediatorResult.Success(
                        endOfPaginationReached = lastItem != null
                    )
                }
            }

            if (!connectivityManager.isOnline()) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            val chats = firebaseChats.getChatsAsList(CHATS_PAGE_SIZE, lastChatId ?: "")

            dataBase.withTransaction {
                if (LoadType.REFRESH == loadType) {
                    dataBase.getChatsDao().clearDb()
                    dataBase.getChatsRemoteKeyDao().clearAllChatKeys()
                }

                val chatIdNew = chats.firstOrNull()?.chat_id
                val newKeys = chats.map {
                    ChatRemoteKeyModel(
                        id = it.chat_id,
                        previousChatId = if (LoadType.REFRESH == loadType) null else lastChatId,
                        nextChatId = chatIdNew
                    )
                }

                dataBase.getChatsRemoteKeyDao().addNextChatKeys(newKeys)
                dataBase.getChatsDao().insertChats(chats)
            }

            //
            return MediatorResult.Success(endOfPaginationReached = true)
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ChatLocalModel>): ChatRemoteKeyModel? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { chat ->
            dataBase.getChatsRemoteKeyDao().getChatKeyById(chat.chat_id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ChatLocalModel>): ChatRemoteKeyModel? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { chat ->
                dataBase.getChatsRemoteKeyDao().getChatKeyById(chat.chat_id)
            }
    }
}