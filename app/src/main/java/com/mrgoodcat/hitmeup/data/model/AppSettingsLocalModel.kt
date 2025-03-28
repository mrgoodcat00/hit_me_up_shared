package com.mrgoodcat.hitmeup.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "app_settings")
data class AppSettingsLocalModel(
    @PrimaryKey
    @ColumnInfo(name = "object_id") val id: Int = 0,

    @ColumnInfo(name = "current_screen")
    val currentScreen: String = "",

    @ColumnInfo(name = "user_verified")
    val isUserVerified: Boolean = true,

    @ColumnInfo(name = "current_opened_chat_id")
    val currentOpenedChatId: String = "",
) : Parcelable