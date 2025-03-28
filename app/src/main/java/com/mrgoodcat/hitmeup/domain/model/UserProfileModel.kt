package com.mrgoodcat.hitmeup.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfileModel(
    val userId: String = "",
    val userLastName: String = "",
    val userFirstName: String = "",
    val userAvatar: String = "",
    val userPhoneNumber: String = "",
    val userFcmToken: String = "",
    val userEmail: String = "",
    val userLastSeen: Long = 0,
    val userFriends: Map<String, Map<String, String>> = emptyMap(),
    val userChats: Map<String, Map<String, String>> = emptyMap(),
) : Parcelable