package com.mrgoodcat.hitmeup.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel

@Dao
interface FriendsDao {
    @Query("SELECT * FROM friends ")
    fun getFriends(): PagingSource<Int, FriendLocalModel>

    @Query("SELECT * FROM friends WHERE user_id = :id LIMIT 1")
    suspend fun getFriend(id: String): FriendLocalModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(users: FriendLocalModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(users: List<FriendLocalModel>)

    @Query("DELETE FROM friends WHERE user_id = :id")
    suspend fun deleteFriend(id: String)

    @Delete
    suspend fun deleteFriend(friend: FriendLocalModel)

    @Query("DELETE FROM friends ")
    suspend fun clearDb()
}