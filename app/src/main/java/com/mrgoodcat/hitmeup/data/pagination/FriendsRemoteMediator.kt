package com.mrgoodcat.hitmeup.data.pagination

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.db.HitMeUpDatabase
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.FriendRemoteKeyModel
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CONTACTS_PAGE_SIZE
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserLocalModel
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import timber.log.Timber
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class FriendsRemoteMediator(
    private val dataBase: HitMeUpDatabase,
    private val firebaseUsersApi: FirebaseUsersApi,
    private val connectivityManager: ConnectivityStateManager
) : RemoteMediator<Int, FriendLocalModel>() {

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)
        val lastKeyCreated = dataBase.getFriendsRemoteKeyDao().getCreationTime() ?: 0L

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
        state: PagingState<Int, FriendLocalModel>
    ): MediatorResult {
        return try {
            Timber.d("FriendsRemoteMediator loadType:$loadType")

            val lastUserId = when (loadType) {
                LoadType.REFRESH -> ""

                LoadType.PREPEND -> {
                    if (state.anchorPosition == null) {
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }

                    val item = state.closestItemToPosition(state.anchorPosition ?: 0)

                    if (item == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    val remKey = dataBase.withTransaction {
                        dataBase.getFriendsRemoteKeyDao().getFriendKeyById(item.user_id)
                    }

                    remKey?.prevFriendId ?: ""
                }

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()

                    if (lastItem == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    val remKey = dataBase.withTransaction {
                        dataBase.getFriendsRemoteKeyDao().getFriendKeyById(lastItem.user_id)
                    }

                    remKey?.nextFriendId ?: ""
                }
            }

            if (!connectivityManager.isOnline()) {
                return MediatorResult.Success(endOfPaginationReached = lastUserId.isEmpty())
            }

            val friends = firebaseUsersApi.getFriendsAsList(CONTACTS_PAGE_SIZE, lastUserId)
                .filter {
                    !it.userDeleted
                }

            if (friends.isEmpty()) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            dataBase.withTransaction {
                if (LoadType.REFRESH == loadType) {
                    dataBase.getFriendDao().clearDb()
                    dataBase.getFriendsRemoteKeyDao().clearAllFriendKeys()
                }

                val userIdNew = friends.last().user_id

                val newKeys = friends.map {
                    FriendRemoteKeyModel(
                        id = it.user_id,
                        prevFriendId = lastUserId,
                        nextFriendId = userIdNew
                    )
                }

                dataBase.getFriendsRemoteKeyDao().addNextFriendKeys(newKeys)
                dataBase.getFriendDao().insertFriends(friends)

                val localUsersList = friends.map { it.toUserLocalModel() }
                dataBase.getUserDao().insertUsers(localUsersList)
            }

            MediatorResult.Success(endOfPaginationReached = friends.isEmpty())
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }

    }
}

