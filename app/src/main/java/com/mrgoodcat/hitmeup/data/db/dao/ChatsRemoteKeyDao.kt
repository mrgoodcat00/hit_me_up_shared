package com.mrgoodcat.hitmeup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrgoodcat.hitmeup.data.model.ChatRemoteKeyModel

@Dao
interface ChatsRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNextChatKeys(keys: List<ChatRemoteKeyModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNextChatKey(keys: ChatRemoteKeyModel)

    @Query("DELETE FROM chat_remote_key")
    suspend fun clearAllChatKeys()

    @Query("DELETE FROM chat_remote_key WHERE id = :chatId")
    suspend fun clearAllChatKeysByChatId(chatId: String)

    @Query("SELECT created_at FROM chat_remote_key ORDER BY created_at DESC LIMIT 1")
    suspend fun getCreationTime(): Long?

    @Query("SELECT * FROM chat_remote_key ORDER BY created_at DESC LIMIT 1")
    suspend fun getLastCreatedChat(): ChatRemoteKeyModel?

    @Query("SELECT * FROM chat_remote_key WHERE id = :chatId LIMIT 1")
    suspend fun getChatKeyById(chatId: String): ChatRemoteKeyModel?
}