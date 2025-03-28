package com.mrgoodcat.hitmeup.domain.model

import com.google.firebase.messaging.RemoteMessage

data class FirebasePushMessageModel(
    val senderId: String = "",
    val senderName: String = "",
    val messageText: String = "",
    val chatId: String = "",
    val messageTimestamp: String = "",
    val pushInternalObject: RemoteMessage.Notification? = null
)