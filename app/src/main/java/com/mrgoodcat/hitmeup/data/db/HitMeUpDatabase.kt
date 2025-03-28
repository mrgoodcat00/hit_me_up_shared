package com.mrgoodcat.hitmeup.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrgoodcat.hitmeup.data.db.dao.AppSettingsDao
import com.mrgoodcat.hitmeup.data.db.dao.ChatsDao
import com.mrgoodcat.hitmeup.data.db.dao.ChatsRemoteKeyDao
import com.mrgoodcat.hitmeup.data.db.dao.CompanyInfoDao
import com.mrgoodcat.hitmeup.data.db.dao.FriendsDao
import com.mrgoodcat.hitmeup.data.db.dao.FriendsRemoteKeyDao
import com.mrgoodcat.hitmeup.data.db.dao.MessageDao
import com.mrgoodcat.hitmeup.data.db.dao.MessagesRemoteKeysDao
import com.mrgoodcat.hitmeup.data.db.dao.UserProfileDao
import com.mrgoodcat.hitmeup.data.db.dao.UsesDao
import com.mrgoodcat.hitmeup.data.model.AppSettingsLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatRemoteKeyModel
import com.mrgoodcat.hitmeup.data.model.CompanyInfoLocalModel
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.FriendRemoteKeyModel
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel
import com.mrgoodcat.hitmeup.data.model.MessageRemoteKeyModel
import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel

@Database(
    entities = [
        UserLocalModel::class,
        CompanyInfoLocalModel::class,
        ChatLocalModel::class,
        MessageLocalModel::class,
        MessageRemoteKeyModel::class,
        ChatRemoteKeyModel::class,
        FriendRemoteKeyModel::class,
        FriendLocalModel::class,
        UserProfileLocalModel::class,
        AppSettingsLocalModel::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(TypeRoomConverter::class)
abstract class HitMeUpDatabase : RoomDatabase() {

    abstract fun getUserDao(): UsesDao

    abstract fun getCompanyDao(): CompanyInfoDao

    abstract fun getMessageDao(): MessageDao

    abstract fun getChatsDao(): ChatsDao

    abstract fun getFriendDao(): FriendsDao

    abstract fun getMessagesRemoteKeysDao(): MessagesRemoteKeysDao

    abstract fun getChatsRemoteKeyDao(): ChatsRemoteKeyDao

    abstract fun getFriendsRemoteKeyDao(): FriendsRemoteKeyDao

    abstract fun getProfileDao(): UserProfileDao

    abstract fun getAppSettingsDao(): AppSettingsDao
}