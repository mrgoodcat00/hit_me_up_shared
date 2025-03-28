package com.mrgoodcat.hitmeup.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageLocalModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageLocalModel>)

    @Query("SELECT * FROM message WHERE id == :id LIMIT 1")
    suspend fun getMessage(id: String): MessageLocalModel?

    @Query("SELECT * from message WHERE chat_id == :chatId ORDER BY timestamp DESC")
    fun getMessages(chatId: String): PagingSource<Int, MessageLocalModel>

    @Update
    suspend fun updateMessage(message: MessageLocalModel)

    @Query("DELETE FROM message WHERE chat_id = :chatId")
    suspend fun deleteMessage(chatId: String)

    @Delete
    suspend fun deleteMessage(message: MessageLocalModel)

    @Query("DELETE FROM message")
    suspend fun clearMessagesFromDb()
}