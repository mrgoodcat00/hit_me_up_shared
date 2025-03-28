package com.mrgoodcat.hitmeup.data.push_notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.RemoteMessage
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_CHAT_ID_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_CONTENT_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_PUSH_OBJECT_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_SENDER_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.NOTIFICATION_CHANNEL_DEFAULT_ID
import com.mrgoodcat.hitmeup.domain.model.FirebasePushMessageModel
import com.mrgoodcat.hitmeup.presentation.MainActivity
import com.squareup.moshi.Moshi
import javax.inject.Inject

class PushNotificationBuilder @Inject constructor(private val moshi: Moshi) {

    fun buildNotificationWithNewMessage(
        context: Context,
        pushModel: FirebasePushMessageModel
    ): NotificationCompat.Builder {
        val pushObject = pushModel.pushInternalObject
        val bundle = bundleOf(
            BROADCAST_CHAT_ID_EXTRA_KEY to pushModel.chatId,
            BROADCAST_SENDER_EXTRA_KEY to pushModel.senderId,
            BROADCAST_MESSAGE_CONTENT_EXTRA_KEY to pushModel.messageText,
            BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY to pushModel.messageTimestamp,
        )

        try {
            val stringFromPush = moshi
                .adapter(RemoteMessage.Notification::class.java)
                .toJson(pushModel.pushInternalObject)

            bundle.putString(BROADCAST_MESSAGE_PUSH_OBJECT_EXTRA_KEY, stringFromPush)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val intentOpenApp = Intent(context, MainActivity::class.java)
        intentOpenApp.putExtras(bundle)
        intentOpenApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntentOpenApp = PendingIntent
            .getActivity(context.applicationContext, 0, intentOpenApp, pendingFlag)

        val builder = NotificationCompat
            .Builder(context, pushObject?.channelId ?: NOTIFICATION_CHANNEL_DEFAULT_ID)
            .setColor(context.resources.getColor(R.color.light_blue_top_icon, null))
            .setContentTitle(pushObject?.title ?: context.getString(R.string.no_title))
            .setContentText(pushObject?.body ?: context.getString(R.string.no_text))
            .setPriority(pushObject?.notificationPriority ?: NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntentOpenApp)
            .setAutoCancel(true)
            .setExtras(bundle)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.ic_notification_clipart)
        } else {
            builder.setSmallIcon(R.drawable.ic_main_logo)
        }

        return builder
    }

}