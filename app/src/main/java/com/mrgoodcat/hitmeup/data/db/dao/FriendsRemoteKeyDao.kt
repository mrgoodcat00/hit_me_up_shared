package com.mrgoodcat.hitmeup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrgoodcat.hitmeup.data.model.FriendRemoteKeyModel

@Dao
interface FriendsRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNextFriendKeys(keys: List<FriendRemoteKeyModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNextFriendKey(keys: FriendRemoteKeyModel)

    @Query("DELETE FROM friend_remote_key")
    suspend fun clearAllFriendKeys()

    @Query("DELETE FROM friend_remote_key WHERE id = :friendId")
    suspend fun deleteFriendKeysById(friendId: String)

    @Query("SELECT created_at FROM friend_remote_key ORDER BY created_at DESC LIMIT 1")
    suspend fun getCreationTime(): Long?

    @Query("SELECT * FROM friend_remote_key ORDER BY created_at DESC LIMIT 1")
    suspend fun getLastCreatedFriend(): FriendRemoteKeyModel?

    @Query("SELECT * FROM friend_remote_key WHERE id = :userId LIMIT 1")
    suspend fun getFriendKeyById(userId: String): FriendRemoteKeyModel?
}