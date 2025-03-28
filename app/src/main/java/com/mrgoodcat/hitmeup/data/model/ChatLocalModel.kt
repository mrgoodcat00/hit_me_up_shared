package com.mrgoodcat.hitmeup.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@Keep
@Entity(tableName = "chat")
@IgnoreExtraProperties
data class ChatLocalModel(
    @PrimaryKey
    @ColumnInfo(name = "chat_id") val chat_id: String = "",
    @ColumnInfo(name = "owner") val owner: String = "",
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "last_message_text") val last_message_text: String = "",
    @ColumnInfo(name = "last_message_timestamp") val last_message_timestamp: Long = 0,
    @ColumnInfo(name = "last_message_sender_id") val last_message_sender: String = "",
    @ColumnInfo(name = "chat_avatar") val chat_avatar: String = "",
    @ColumnInfo(name = "participant_ids") val participant_ids: Map<String, Boolean> = emptyMap(),
    @ColumnInfo(name = "unreaded_count") val unreadedCounter: Int = 0
) : Serializable {

    constructor() : this(
        chat_id = "",
        owner = "",
        title = "",
        last_message_text = "",
        last_message_timestamp = 0,
        last_message_sender = "",
        chat_avatar = "",
        participant_ids = emptyMap(),
        unreadedCounter = 0
    )

    @Exclude
    fun getMapToCreateChat(): Map<String, Any?> {
        return mapOf(
            "chat_id" to chat_id,
            "owner" to owner,
            "title" to title,
            "participant_ids" to participant_ids,
            "last_message_timestamp" to last_message_timestamp,
        )
    }
}

