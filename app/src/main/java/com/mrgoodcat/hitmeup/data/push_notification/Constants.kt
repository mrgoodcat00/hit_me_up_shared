package com.mrgoodcat.hitmeup.data.push_notification

class Constants {
    companion object {
        const val NOTIFICATION_CHANNEL_DEFAULT_ID: String = "notification_channel_default"
        const val NOTIFICATION_CHANNEL_DEFAULT_NAME: String = "HitMeUp messages"

        const val BROADCAST_CHAT_ID_EXTRA_KEY = "chat_id_extra_key"
        const val BROADCAST_SENDER_EXTRA_KEY = "sender_id_extra_key"
        const val BROADCAST_MESSAGE_CONTENT_EXTRA_KEY = "message_content_extra_key"
        const val BROADCAST_MESSAGE_TIMESTAMP_EXTRA_KEY = "message_timestamp_extra_key"
        const val BROADCAST_MESSAGE_PUSH_OBJECT_EXTRA_KEY = "message_push_object_extra_key"

        const val BROADCAST_MESSAGE_NEW_CHAT_MESSAGE_INTENT_FILTER =
            "com.mrgoodcat.hitmeup.broadcast.message"
    }
}