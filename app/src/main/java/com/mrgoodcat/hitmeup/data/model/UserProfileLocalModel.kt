package com.mrgoodcat.hitmeup.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "profile")
data class UserProfileLocalModel(
    @PrimaryKey
    @ColumnInfo(name = "user_id") val user_id: String = "",
    @ColumnInfo(name = "user_last_name") val user_last_name: String = "",
    @ColumnInfo(name = "user_first_name") val user_first_name: String = "",
    @ColumnInfo(name = "user_avatar") val user_avatar: String = "",
    @ColumnInfo(name = "user_phone") val user_phone: String = "",
    @ColumnInfo(name = "user_fcm_token") val user_fcm_token: String = "",
    @ColumnInfo(name = "user_email") val user_email: String = "",
    @ColumnInfo(name = "user_last_seen") val user_last_seen: Long = 0,
    @ColumnInfo(name = "user_friends") val user_friends: Map<String, Map<String, String>> = emptyMap(),
    @ColumnInfo(name = "user_chats") val user_chats: Map<String, Map<String, String>> = emptyMap(),
) : Parcelable