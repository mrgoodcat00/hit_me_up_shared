package com.mrgoodcat.hitmeup.data.push_notification

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_CHAT_ID_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_CONTENT_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_NEW_CHAT_MESSAGE_INTENT_FILTER
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_PUSH_OBJECT_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_SENDER_EXTRA_KEY
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var moshi: Moshi

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Timber.d("onNewToken:$p0")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Timber.d("onMessageReceived $message")

        val chatIdFromMessage = message.data[BROADCAST_CHAT_ID_EXTRA_KEY]

        if (chatIdFromMessage.isNullOrEmpty()) {
            Timber.d("chatIdFromMessage isNullOrEmpty")
            return
        }

        val senderIdFromMessage = message.data[BROADCAST_SENDER_EXTRA_KEY]
        val messageContentFromMessage = message.data[BROADCAST_MESSAGE_CONTENT_EXTRA_KEY]
        val messageTimestampFromMessage = message.data[BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY]

        val intent = Intent(BROADCAST_MESSAGE_NEW_CHAT_MESSAGE_INTENT_FILTER)

        try {
            val pushObjectFromMessage = moshi
                .adapter(RemoteMessage.Notification::class.java)
                .toJson(message.notification)

            intent.putExtra(BROADCAST_MESSAGE_PUSH_OBJECT_EXTRA_KEY, pushObjectFromMessage)
            Timber.d("pushObjectFromMessage $pushObjectFromMessage")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        intent.putExtra(BROADCAST_CHAT_ID_EXTRA_KEY, chatIdFromMessage)
        intent.putExtra(BROADCAST_SENDER_EXTRA_KEY, senderIdFromMessage)
        intent.putExtra(BROADCAST_MESSAGE_CONTENT_EXTRA_KEY, messageContentFromMessage)
        intent.putExtra(BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY, messageTimestampFromMessage)
        intent.setPackage(applicationContext.packageName)

        sendBroadcast(intent)
    }
}