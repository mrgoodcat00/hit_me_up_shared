package com.mrgoodcat.hitmeup.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "chat_remote_key")
data class ChatRemoteKeyModel(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "prev_chat_id") val previousChatId: String?,
    @ColumnInfo(name = "next_chat_id") val nextChatId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
) : Parcelable