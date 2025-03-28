package com.mrgoodcat.hitmeup.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrgoodcat.hitmeup.domain.model.MessageContentType
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "message")
@Parcelize
data class MessageLocalModel(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String = "",
    @ColumnInfo(name = "sender") val sender: String = "",
    @ColumnInfo(name = "text") val text: String = "",
    @ColumnInfo(name = "content") val content: Map<String, String> = emptyMap(),
    @ColumnInfo(name = "timestamp") val timestamp: Long = 0,
    @ColumnInfo(name = "chat_id") val chat_id: String = "",
) : Comparable<MessageLocalModel>, Parcelable {
    constructor() : this("", "", "", emptyMap(), 0, "")

    override fun compareTo(other: MessageLocalModel): Int {
        return if (other.timestamp != this.timestamp) {
            if (other.timestamp > this.timestamp) {
                1
            } else {
                -1
            }
        } else {
            0
        }
    }

    companion object {
        fun createMessageContent(data: MessageContentType): Map<String, String> {
            return when (data) {
                is MessageContentType.SimpleText -> {
                    mapOf(
                        "text" to data.text,
                        "type" to data.type
                    )
                }

                is MessageContentType.TextWithImage -> {
                    mapOf(
                        "text" to data.text,
                        "type" to data.type,
                        "image" to data.image
                    )
                }

                is MessageContentType.Image -> {
                    mapOf(
                        "type" to data.type,
                        "image" to data.image
                    )
                }
            }
        }
    }
}