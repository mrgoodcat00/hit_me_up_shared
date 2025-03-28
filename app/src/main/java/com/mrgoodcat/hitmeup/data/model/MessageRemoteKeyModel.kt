package com.mrgoodcat.hitmeup.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "message_remote_key")
data class MessageRemoteKeyModel(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "chat_id") val chatId: String,
    @ColumnInfo(name = "prev_timestamp") val previousTimestamp: Long?,
    @ColumnInfo(name = "next_timestamp") val nextTimestamp: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
) : Parcelable