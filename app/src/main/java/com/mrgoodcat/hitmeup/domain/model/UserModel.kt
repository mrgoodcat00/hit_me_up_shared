package com.mrgoodcat.hitmeup.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class UserModel(
    val user_id: String = "",
    val user_first_name: String = "",
    val user_last_name: String = "",
    val user_avatar: String = "",
    val user_phone: String = "",
    val user_fcm_token: String = "",
    val user_email: String = "",
    val user_last_seen: Long = 0,
    val user_friends: Map<String, Map<String, String>> = emptyMap(),
    val user_chats: Map<String, Map<String, String>> = emptyMap()
) : Parcelable