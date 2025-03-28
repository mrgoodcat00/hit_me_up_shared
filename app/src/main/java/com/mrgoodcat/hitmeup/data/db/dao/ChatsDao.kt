package com.mrgoodcat.hitmeup.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel

@Dao
interface ChatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatLocalModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatLocalModel>)

    @Query("SELECT * FROM chat WHERE chat_id == :id LIMIT 1")
    suspend fun getChat(id: String): ChatLocalModel?

    @Query("SELECT * FROM chat ORDER BY last_message_timestamp DESC")
    fun getChats(): PagingSource<Int, ChatLocalModel>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateChat(chat: ChatLocalModel)

    @Delete
    suspend fun deleteChat(chat: ChatLocalModel)

    @Query("DELETE FROM chat")
    suspend fun clearDb()

}