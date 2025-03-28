package com.mrgoodcat.hitmeup.data.pagination

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.db.HitMeUpDatabase
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel
import com.mrgoodcat.hitmeup.data.model.MessageRemoteKeyModel
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.MESSAGES_PAGE_SIZE
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.model.extensions.toMessagesLocalModel
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessageDirection.DESC
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessagesApi
import timber.log.Timber
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class MessagesRemoteMediator(
    private val chatId: String,
    private val dataBase: HitMeUpDatabase,
    private val firebaseMessages: FirebaseMessagesApi,
    private val connectivityManager: ConnectivityStateManager
) : RemoteMediator<Int, MessageLocalModel>() {

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)
        val lastKeyCreated = dataBase.getMessagesRemoteKeysDao().getCreationTime(chatId) ?: 0L

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
        state: PagingState<Int, MessageLocalModel>
    ): MediatorResult {
        return try {
            Timber.d("MessagesRemoteMediator loadType:$loadType")

            val lastTimestamp = when (loadType) {
                LoadType.REFRESH -> {
                    System.currentTimeMillis()
                }

                LoadType.PREPEND -> {
                    val firstItem = getRemoteKeyForFirstItem(state)
                    val prevKey = firstItem?.previousTimestamp

                    Timber.d("firstItem: $firstItem prevKey:$prevKey")

                    if (prevKey == null || prevKey == 0L) return MediatorResult.Success(
                        endOfPaginationReached = true
                    ) else prevKey
                }

                LoadType.APPEND -> {
                    val lastItem = getRemoteKeyForLastItem(state)
                    val nextKey = lastItem?.nextTimestamp

                    Timber.d("lastItem: $lastItem nextKey:$nextKey")

                    if (nextKey == null || nextKey == 0L) return MediatorResult.Success(
                        endOfPaginationReached = true
                    ) else nextKey
                }
            }

            if (!connectivityManager.isOnline()) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            val messages = firebaseMessages.getMessagesAsList(
                chatId, MESSAGES_PAGE_SIZE, lastTimestamp, DESC
            ).asReversed()

            dataBase.withTransaction {
                if (LoadType.REFRESH === loadType) {
                    dataBase.getMessageDao().deleteMessage(chatId)
                    dataBase.getMessagesRemoteKeysDao().deleteMessageKeysByChatId(chatId)
                }

                val timestampNew = messages.lastOrNull()?.toMessagesLocalModel()?.timestamp

                Timber.d("transaction timestampNew:$timestampNew messages:${messages.size}")

                val newKeys = messages.map {
                    MessageRemoteKeyModel(
                        id = it.id,
                        chatId = chatId,
                        previousTimestamp = if (LoadType.REFRESH === loadType) null else lastTimestamp,
                        nextTimestamp = timestampNew
                    )
                }
                dataBase.getMessagesRemoteKeysDao().addNextMessageKeys(newKeys)

                val map = messages.map { it.toMessagesLocalModel() }

                dataBase.getMessageDao().insertMessages(map)
            }

            MediatorResult.Success(endOfPaginationReached = messages.isEmpty())
        } catch (exception: Exception) {
            exception.printStackTrace()
            MediatorResult.Error(exception)
        }

    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, MessageLocalModel>): MessageRemoteKeyModel? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { message ->
            dataBase.getMessagesRemoteKeysDao().getMessageKeyById(message.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, MessageLocalModel>): MessageRemoteKeyModel? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { message ->
                dataBase.getMessagesRemoteKeysDao().getMessageKeyById(message.id)
            }
    }
}