package com.mrgoodcat.hitmeup.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.data.repostory.ContactsUpdateResult
import kotlinx.coroutines.flow.Flow

interface FirebaseUsersApi {
    suspend fun getFriendsAsList(
        pageSize: Int,
        lastItem: String,
    ): List<FriendLocalModel>

    suspend fun getUserById(userId: String): UserLocalModel

    suspend fun globalsSearch(query: String): List<FriendLocalModel>

    suspend fun createUser(user: FirebaseUser): UserLocalModel

    suspend fun updateUser(user: UserLocalModel): UserLocalModel

    suspend fun subscribeToContactsList(userId: String): Flow<ContactsUpdateResult>

    suspend fun getFcmToken(userId: String): String

    suspend fun updateLastSeenTime(userId: String): Long

    suspend fun updateFcmToken(userId: String, token: String?): Boolean

    suspend fun deleteUserInDbApi(userId: String): Boolean

    suspend fun deleteUserFirebaseAuth(userId: String): Boolean
}