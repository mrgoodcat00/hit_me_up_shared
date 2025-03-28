package com.mrgoodcat.hitmeup.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatModel(
    val id: String = "",
    val owner: String = "",
    val title: String = "",
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = 0,
    val lastMessageSender: String = "",
    val chatAvatar: String = "",
    val participantIds: Map<String, Boolean> = emptyMap(),
    val unreadedCounter: Int = 0
) : Parcelable