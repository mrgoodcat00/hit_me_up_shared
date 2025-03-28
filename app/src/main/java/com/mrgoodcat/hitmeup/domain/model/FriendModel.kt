package com.mrgoodcat.hitmeup.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FriendModel(
    val useId: String = "",
    val userFirstName: String = "",
    val userLastName: String = "",
    val userAvatar: String = "",
    val userFcmToken: String = "",
    val userPhone: String = "",
    val userEmail: String = "",
    val userLastSeen: Long = 0,
    val userFriends: Map<String, Map<String, String>> = emptyMap(),
    val userChats: Map<String, Map<String, String>> = emptyMap()
) : Parcelable