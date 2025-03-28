package com.mrgoodcat.hitmeup.domain.usecase.contacts

import androidx.room.withTransaction
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.FriendRemoteKeyModel
import com.mrgoodcat.hitmeup.data.repostory.ContactsUpdateResult
import com.mrgoodcat.hitmeup.domain.model.extensions.toFriendLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

open class SubscribeOnContactsListChangesUseCase @Inject constructor(
    private val dbRepository: DbRepository,
    private val usersApi: FirebaseUsersApi
) {
    suspend fun execute() {
        val userProfile = dbRepository.getUserProfile() ?: return

        usersApi
            .subscribeToContactsList(userProfile.user_id)
            .filter {
                if (it is ContactsUpdateResult.OnAdded) {
                    (dbRepository.getFriend(it.friend.user_id) == null)
                } else {
                    true
                }
            }
            .collect {
                when (it) {
                    is ContactsUpdateResult.OnAdded -> {
                        addChatIfIsNotExists(it.friend)
                    }

                    is ContactsUpdateResult.OnRemoved -> {
                        removeDeletedFriend(it.friend)
                    }

                    is ContactsUpdateResult.OnUpdated -> {
                        prependFriendInContactList(it.friend)
                    }
                }
            }


    }

    private suspend fun addChatIfIsNotExists(friendModel: FriendLocalModel) {
        val friend = usersApi
            .getUserById(friendModel.user_id)
            .toFriendLocalModel()

        if (friend.userDeleted) {
            return
        }

        prependFriendInContactList(friend)
    }

    private suspend fun removeDeletedFriend(friendModel: FriendLocalModel) {
        dbRepository.getDbInstance().withTransaction {
            dbRepository.deleteFriend(friendModel)
            dbRepository.deleteFriendKeyById(friendModel.user_id)
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