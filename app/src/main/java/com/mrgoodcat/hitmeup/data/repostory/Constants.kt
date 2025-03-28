package com.mrgoodcat.hitmeup.data.repostory

class Constants {
    companion object {
        const val CHATS_DB_KEY: String = "CHATS"
        const val MESSAGES_DB_KEY: String = "MESSAGES"
        const val MESSAGES_TIMESTAMP_DB_KEY: String = "timestamp"
        const val USERS_DB_KEY: String = "USERS"
        const val COMPANY_INFO: String = "COMPANY_INFO"
        const val FCM_TOKENS_DB_KEY: String = "FCM_TOKENS"
        const val USER_CHATS_DB_KEY: String = "user_chats"
        const val USER_CHAT_ID_DB_KEY: String = "chat_id"
        const val USER_LAST_SEEN_DB_KEY: String = "user_last_seen"
        const val USER_CHAT_LAST_MESSAGE_TIMESTAMP_DB_KEY: String = "last_message_timestamp"
        const val USER_CHAT_LAST_MESSAGE_TEXT_DB_KEY: String = "last_message_text"
        const val USER_CHAT_LAST_MESSAGE_SENDER_DB_KEY: String = "last_message_sender"
        const val USER_CHAT_PARTICIPANTS_DB_KEY: String = "participant_ids"
        const val USER_FRIENDS_DB_KEY: String = "user_friends"
        const val USER_FRIENDS_USER_ID_DB_KEY: String = "user_id"
        const val MEDIA_CHATS_DB_KEY: String = "chats"
        const val MEDIA_MESSAGES_DB_KEY: String = "messages"
        const val MEDIA_USERS_DB_KEY: String = "users"
        const val CURRENT_NAMESPACE: String = "mrgoodcat"

        const val CHATS_PAGE_SIZE = 55
        const val CONTACTS_PAGE_SIZE = 35
        const val MESSAGES_PAGE_SIZE = 40
    }
}