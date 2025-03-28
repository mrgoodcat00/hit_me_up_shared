package com.mrgoodcat.hitmeup.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "friend_remote_key")
data class FriendRemoteKeyModel(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "prev_friend_id") val prevFriendId: String,
    @ColumnInfo(name = "next_friend_id") val nextFriendId: String,
    @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
) : Parcelable