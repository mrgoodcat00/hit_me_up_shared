package com.mrgoodcat.hitmeup.domain.model.extensions

import com.mrgoodcat.hitmeup.data.model.AppSettingsLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.InternalMessageLocalContent
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel
import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel
import com.mrgoodcat.hitmeup.domain.model.AppSettingsModel
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.FriendModel
import com.mrgoodcat.hitmeup.domain.model.MessageContentType
import com.mrgoodcat.hitmeup.domain.model.MessageContentType.Companion.MESSAGE_CONTENT_TYPE_IMAGE
import com.mrgoodcat.hitmeup.domain.model.MessageContentType.Companion.MESSAGE_CONTENT_TYPE_SIMPLE_TEXT
import com.mrgoodcat.hitmeup.domain.model.MessageContentType.Companion.MESSAGE_CONTENT_TYPE_TEXT_WITH_IMAGE
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.domain.model.UserProfileModel

fun FriendModel.toFriendLocalModel(): FriendLocalModel {
    return FriendLocalModel(
        user_id = useId,
        user_first_name = userFirstName,
        user_last_name = userLastName,
        user_avatar = userAvatar,
        user_fcm_token = userFcmToken,
        user_phone = userPhone,
        user_email = userEmail,
        user_friends = userFriends,
        user_chats = userChats,
        user_last_seen = userLastSeen
    )
}

fun FriendModel.toUserLocalModel(): UserLocalModel {
    return UserLocalModel(
        user_id = useId,
        user_first_name = userFirstName,
        user_last_name = userLastName,
        user_avatar = userAvatar,
        user_fcm_token = userFcmToken,
        user_phone = userPhone,
        user_email = userEmail,
        user_friends = userFriends,
        user_chats = userChats,
        user_last_seen = userLastSeen
    )
}

fun AppSettingsLocalModel?.toAppSettingsModel(): AppSettingsModel? {
    if (this == null) {
        return null
    }

    return AppSettingsModel(
        id = this.id,
        currentScreen = this.currentScreen,
        isUserVerified = this.isUserVerified,
        currentOpenedChatId = this.currentOpenedChatId
    )
}

fun AppSettingsModel?.toAppSettingsLocalModel(): AppSettingsLocalModel? {

    if (this == null) {
        return null
    }

    return AppSettingsLocalModel(
        id = this.id,
        currentScreen = this.currentScreen,
        isUserVerified = this.isUserVerified,
        currentOpenedChatId = this.currentOpenedChatId
    )
}

fun ChatLocalModel?.toChatModel(): ChatModel? {
    if (this == null) {
        return null
    }
    return ChatModel(
        id = chat_id,
        owner = owner,
        title = title,
        lastMessageText = last_message_text,
        lastMessageTimestamp = last_message_timestamp,
        lastMessageSender = last_message_sender,
        chatAvatar = chat_avatar,
        participantIds = participant_ids,
        unreadedCounter = unreadedCounter,
    )
}

fun ChatModel.toChatLocalModel(): ChatLocalModel {
    return ChatLocalModel(
        chat_id = id,
        owner = owner,
        title = title,
        last_message_text = lastMessageText,
        last_message_timestamp = lastMessageTimestamp,
        last_message_sender = lastMessageSender,
        chat_avatar = chatAvatar,
        participant_ids = participantIds,
        unreadedCounter = unreadedCounter,
    )
}

fun FriendLocalModel.toFriendModel(): FriendModel {
    return FriendModel(
        useId = user_id,
        userLastName = user_last_name,
        userFirstName = user_first_name,
        userAvatar = user_avatar,
        userFcmToken = user_fcm_token,
        userPhone = user_phone,
        userEmail = user_email,
        userFriends = user_friends,
        userChats = user_chats,
        userLastSeen = user_last_seen
    )
}

fun FriendLocalModel.toUserLocalModel(): UserLocalModel {
    return UserLocalModel(
        user_id = user_id,
        user_last_name = user_last_name,
        user_first_name = user_first_name,
        user_avatar = user_avatar,
        user_fcm_token = user_fcm_token,
        user_phone = user_phone,
        user_email = user_email,
        user_friends = user_friends,
        user_chats = user_chats,
        user_last_seen = user_last_seen
    )
}

fun MessageModel.putObjectToContent(data: InternalMessageLocalContent): Map<String, String> {
    return when (data) {
        is InternalMessageLocalContent.SimpleText -> {
            mapOf(
                "text" to data.text,
                "type" to data.type
            )
        }

        is InternalMessageLocalContent.TextWithImage -> {
            mapOf(
                "text" to data.text,
                "type" to data.type,
                "image" to data.image
            )
        }

        is InternalMessageLocalContent.Image -> {
            mapOf(
                "type" to data.type,
                "image" to data.image
            )
        }
    }
}

fun MessageModel.toMessagesLocalModel(): MessageLocalModel {
    return MessageLocalModel(
        id = this.id,
        sender = this.sender,
        text = this.text,
        content = this.content,
        timestamp = this.timestamp,
        chat_id = this.chat_id
    )
}


fun MessageModel.getContentToObject(): MessageContentType {
    return when (this.content["type"]) {
        MESSAGE_CONTENT_TYPE_SIMPLE_TEXT -> {
            MessageContentType.SimpleText(
                text = this.content["text"].orEmpty()
            )
        }

        MESSAGE_CONTENT_TYPE_TEXT_WITH_IMAGE -> {
            MessageContentType.TextWithImage(
                text = this.content["text"].orEmpty(),
                image = this.content["image"].orEmpty()
            )
        }

        MESSAGE_CONTENT_TYPE_IMAGE -> {
            MessageContentType.Image(
                image = this.content["image"].orEmpty()
            )
        }

        else -> {
            MessageContentType.SimpleText(
                text = ""
            )
        }
    }
}

fun MessageModel.getContentType(): String {
    return when (this.content["type"]) {
        MESSAGE_CONTENT_TYPE_SIMPLE_TEXT -> {
            this.content["text"].orEmpty()
        }

        MESSAGE_CONTENT_TYPE_IMAGE -> {
            "Photo"
        }

        MESSAGE_CONTENT_TYPE_TEXT_WITH_IMAGE -> {
            "Photo"
        }

        else -> {
            ""
        }
    }
}

fun MessageLocalModel.toMessagesModel(): MessageModel {
    return MessageModel(
        id = this.id,
        sender = this.sender,
        text = this.text,
        content = this.content,
        timestamp = this.timestamp,
        chat_id = this.chat_id
    )
}

fun UserLocalModel.toUserModel(): UserModel {
    return UserModel(
        user_id = user_id,
        user_first_name = user_first_name,
        user_last_name = user_last_name,
        user_avatar = user_avatar,
        user_phone = user_phone,
        user_fcm_token = user_fcm_token,
        user_email = user_email,
        user_friends = user_friends,
        user_chats = user_chats,
        user_last_seen = user_last_seen,
    )
}

fun UserLocalModel.toFriendLocalModel(): FriendLocalModel {
    return FriendLocalModel(
        user_id = user_id,
        user_first_name = user_first_name,
        user_last_name = user_last_name,
        user_avatar = user_avatar,
        user_fcm_token = user_fcm_token,
        user_phone = user_phone,
        user_email = user_email,
        user_friends = user_friends,
        user_chats = user_chats,
        user_last_seen = user_last_seen,
        userDeleted = userDeleted
    )
}

fun UserLocalModel.toUserProfileLocalModel(): UserProfileLocalModel {
    return UserProfileLocalModel(
        user_id = user_id,
        user_first_name = user_first_name,
        user_last_name = user_last_name,
        user_avatar = user_avatar,
        user_fcm_token = user_fcm_token,
        user_phone = user_phone,
        user_email = user_email,
        user_friends = user_friends,
        user_chats = user_chats,
        user_last_seen = user_last_seen
    )
}

fun UserProfileLocalModel.toUserProfileModel(): UserProfileModel {
    return UserProfileModel(
        userId = user_id,
        userFirstName = user_first_name,
        userLastName = user_last_name,
        userAvatar = user_avatar,
        userPhoneNumber = user_phone,
        userFcmToken = user_fcm_token,
        userEmail = user_email,
        userLastSeen = user_last_seen,
        userFriends = user_friends,
        userChats = user_chats
    )
}

fun UserModel.toUserLocalModel(): UserLocalModel {
    return UserLocalModel(
        user_id = user_id,
        user_first_name = user_first_name,
        user_last_name = user_last_name,
        user_avatar = user_avatar,
        user_fcm_token = user_fcm_token,
        user_phone = user_phone,
        user_email = user_email,
        user_friends = user_friends,
        user_chats = user_chats,
        user_last_seen = user_last_seen
    )
}

fun UserModel.toFriendModel(): FriendModel {
    return FriendModel(
        useId = user_id,
        userFirstName = user_first_name,
        userLastName = user_last_name,
        userAvatar = user_avatar,
        userPhone = user_phone,
        userFcmToken = user_fcm_token,
        userEmail = user_email,
        userFriends = user_friends,
        userChats = user_chats,
        userLastSeen = user_last_seen
    )
}

fun UserProfileModel.toUserProfileLocalModel(): UserProfileLocalModel {
    return UserProfileLocalModel(
        user_id = userId,
        user_first_name = userFirstName,
        user_last_name = userLastName,
        user_avatar = userAvatar,
        user_fcm_token = userFcmToken,
        user_phone = userPhoneNumber,
        user_email = userEmail,
        user_last_seen = userLastSeen,
        user_friends = userFriends,
        user_chats = userChats
    )
}