package com.mrgoodcat.hitmeup.domain.usecase

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.google.firebase.messaging.RemoteMessage.Notification
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_CHAT_ID_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_CONTENT_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_NEW_CHAT_MESSAGE_INTENT_FILTER
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_PUSH_OBJECT_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_SENDER_EXTRA_KEY
import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.model.FirebasePushMessageModel
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject


open class RegisterPushBroadcastReceiverUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val moshi: Moshi
) {
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    suspend fun execute(): Flow<FirebasePushMessageModel> {
        return CoroutineScope(ioDispatcher).async {
            callbackFlow {

                val filter = IntentFilter(BROADCAST_MESSAGE_NEW_CHAT_MESSAGE_INTENT_FILTER)
                val broadcastReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        Timber.d(
                            "RegisterPushBroadcastReceiverUseCase ${
                                intent?.extras?.keySet()?.toList().toString()
                            }"
                        )

                        if (intent?.hasExtra(BROADCAST_CHAT_ID_EXTRA_KEY) == true) {

                            var chatFromPush = FirebasePushMessageModel()
                            intent.getStringExtra(BROADCAST_CHAT_ID_EXTRA_KEY)?.let {
                                chatFromPush = chatFromPush.copy(chatId = it)
                            }
                            intent.getStringExtra(BROADCAST_SENDER_EXTRA_KEY)?.let {
                                chatFromPush = chatFromPush.copy(senderId = it)
                            }
                            intent.getStringExtra(BROADCAST_MESSAGE_CONTENT_EXTRA_KEY)?.let {
                                chatFromPush = chatFromPush.copy(messageText = it)
                            }
                            intent.getStringExtra(BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY)?.let {
                                chatFromPush = chatFromPush.copy(messageTimestamp = it)

                            }
                            try {
                                intent.getStringExtra(BROADCAST_MESSAGE_PUSH_OBJECT_EXTRA_KEY)
                                    ?.let {
                                        val fromJson = moshi
                                            .adapter(Notification::class.java)
                                            .fromJson(it)

                                        chatFromPush =
                                            chatFromPush.copy(pushInternalObject = fromJson)
                                    }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            trySend(chatFromPush)
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val flag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Context.RECEIVER_EXPORTED
                    } else {
                        Context.RECEIVER_VISIBLE_TO_INSTANT_APPS
                    }
                    context.registerReceiver(broadcastReceiver, filter, flag)
                } else {
                    context.registerReceiver(broadcastReceiver, filter)
                }

                awaitClose {
                    Timber.d("RegisterPushBroadcastReceiverUseCase awaitClose")
                    context.unregisterReceiver(broadcastReceiver)
                }
            }
        }.await()
    }
}