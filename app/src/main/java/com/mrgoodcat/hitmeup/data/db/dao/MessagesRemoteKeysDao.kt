package com.mrgoodcat.hitmeup.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrgoodcat.hitmeup.data.model.MessageRemoteKeyModel

@Dao
interface MessagesRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNextMessageKeys(keys: List<MessageRemoteKeyModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNextMessageKey(keys: MessageRemoteKeyModel)

    @Query("DELETE FROM message_remote_key")
    suspend fun clearAllMessageKeys()

    @Query("DELETE FROM message_remote_key WHERE chat_id = :chatId")
    suspend fun deleteMessageKeysByChatId(chatId: String)

    @Query("SELECT created_at FROM message_remote_key WHERE chat_id = :chatId GROUP BY chat_id ORDER BY created_at DESC LIMIT 1")
    suspend fun getCreationTime(chatId: String): Long?

    @Query("SELECT * FROM message_remote_key WHERE id = :messageId LIMIT 1")
    suspend fun getMessageKeyById(messageId: String): MessageRemoteKeyModel?
}