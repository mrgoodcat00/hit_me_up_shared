package com.mrgoodcat.hitmeup.data.repostory

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.messaging.FirebaseMessaging
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CURRENT_NAMESPACE
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.FCM_TOKENS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USERS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_FRIENDS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_FRIENDS_USER_ID_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_LAST_SEEN_DB_KEY
import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.model.extensions.toFriendLocalModel
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class FirebaseUsersApiImpl(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FirebaseUsersApi {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseMessaging = FirebaseMessaging.getInstance()
    private var databaseReference: DatabaseReference = database.getReferenceFromUrl(
        context.getString(R.string.firebase_database_url_string)
    )

    private fun fetchUserDataById(userId: String, callback: (UserLocalModel) -> Unit) {
        val singleEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userType =
                    object : GenericTypeIndicator<@JvmSuppressWildcards UserLocalModel>() {}
                Timber.d("user: $snapshot")
                val user = snapshot.getValue(userType) ?: UserLocalModel()
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(UserLocalModel())
            }
        }

        val dbRef = databaseReference
            .child(CURRENT_NAMESPACE)
            .child(USERS_DB_KEY)
            .child(userId)

        try {
            dbRef.addListenerForSingleValueEvent(singleEventListener)
        } catch (exception: Exception) {
            Timber.d("exception: invokeOnCancellation ${exception.message}")
            dbRef.removeEventListener(singleEventListener)
            callback(UserLocalModel())
        }
    }

    override suspend fun getFriendsAsList(pageSize: Int, lastItem: String): List<FriendLocalModel> {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->

                val resultList = ArrayList<FriendLocalModel>()
                var resultSize: Int

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        resultSize = snapshot.children.count()

                        if (resultSize == 0) {
                            continuation.resume(resultList)
                            return
                        }

                        snapshot.children.map {
                            val friendType = object :
                                GenericTypeIndicator<@JvmSuppressWildcards FriendLocalModel>() {}
                            val user = it.getValue(friendType) ?: FriendLocalModel()
                            Timber.d("fetchUserFriends: $it")

                            fetchUserDataById(user.user_id) { userModel ->
                                resultList.add(userModel.toFriendLocalModel())

                                if (resultList.size == resultSize) {
                                    continuation.resume(resultList)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.d("onDataChange: ${error.message}")
                        resultList.addAll(emptyList())
                        continuation.resumeWithException(Exception())
                    }
                }

                val dbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(USERS_DB_KEY)
                    .child(firebaseAuth.currentUser!!.uid)
                    .child(USER_FRIENDS_DB_KEY)

                try {
                    dbRef
                        .orderByChild(USER_FRIENDS_USER_ID_DB_KEY)
                        .limitToFirst(pageSize)
                        .startAfter(lastItem)
                        .addListenerForSingleValueEvent(listener)

                    continuation.invokeOnCancellation {
                        Timber.d("getChatsAsList: invokeOnCancellation")
                        dbRef.removeEventListener(listener)
                    }

                } catch (exception: Exception) {
                    Timber.d("exception: invokeOnCancellation ${exception.message}")
                    resultList.addAll(emptyList())
                    databaseReference.removeEventListener(listener)
                    continuation.resumeWithException(exception)
                }
            }
        }
    }

    override suspend fun getUserById(userId: String): UserLocalModel {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                try {
                    fetchUserDataById(userId) { user ->
                        continuation.resume(user)
                    }

                    continuation.invokeOnCancellation {
                        Timber.d("getUserById: invokeOnCancellation")
                    }
                } catch (e: Exception) {
                    Timber.d("exception: Exception ${e.message}")
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun globalsSearch(query: String): List<FriendLocalModel> {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val dbRef = databaseReference
                        .child(CURRENT_NAMESPACE)
                        .child(USERS_DB_KEY)
                        .orderByChild("user_first_name")
                        .startAt(query)
                        .endAt(query + "\uf8ff")

                    val resultList = ArrayList<FriendLocalModel>()

                    val callback = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Timber.d("globalsSearch: postSnapshot:${dataSnapshot.childrenCount}")
                            if (dataSnapshot.childrenCount.toInt() == 0) {
                                continuation.resume(resultList)
                            }
                            for (postSnapshot in dataSnapshot.children) {
                                try {
                                    val friendType = object :
                                        GenericTypeIndicator<@JvmSuppressWildcards FriendLocalModel>() {}
                                    val user = postSnapshot.getValue(friendType)
                                    Timber.d("globalsSearch: user:${user}")
                                    user?.let { resultList.add(it) }
                                    if (resultList.size == dataSnapshot.childrenCount.toInt()) {
                                        continuation.resume(resultList)
                                    }
                                } catch (e: Exception) {
                                    Timber.d("globalsSearch: $e")
                                }
                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Timber.d("databaseError ${databaseError.toException()}")
                            if (continuation.isActive)
                                continuation.resumeWithException(databaseError.toException())
                        }
                    }

                    dbRef.addValueEventListener(callback)

                    continuation.invokeOnCancellation {
                        Timber.d("globalsSearch: invokeOnCancellation")
                        dbRef.removeEventListener(callback)
                    }

                    Timber.d("globalsSearch: query:$query")
                } catch (exception: Exception) {
                    Timber.d("globalsSearch exception: $exception")
                    continuation.resumeWithException(exception)
                }
            }
        }
    }

    override suspend fun createUser(user: FirebaseUser): UserLocalModel =
        withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                val dogIndex = user.providerData.lastOrNull()?.email?.indexOf('@', 0, true) ?: 0

                var emptyName = user
                    .providerData
                    .lastOrNull()
                    ?.email
                    ?.substring(0, dogIndex)

                emptyName = if (emptyName.isNullOrEmpty()) NO_NAME_FIELD else emptyName

                val name = user.providerData.lastOrNull()?.displayName ?: emptyName
                val email = user.providerData.lastOrNull()?.email ?: NO_EMAIL_FIELD
                var avatar = user.providerData.lastOrNull()?.photoUrl.toString() ?: ""
                val phone = user.providerData.lastOrNull()?.phoneNumber ?: ""

                avatar = if (avatar == "null") "" else avatar

                val userToCreate = UserLocalModel(
                    user_id = user.uid,
                    user_first_name = name,
                    user_last_name = "",
                    user_phone = phone,
                    user_email = email,
                    user_friends = HashMap(),
                    user_chats = HashMap(),
                    user_avatar = avatar,
                    user_last_seen = System.currentTimeMillis(),
                    user_fcm_token = "",
                )

                val dbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(USERS_DB_KEY)

                try {
                    dbRef
                        .child(userToCreate.user_id)
                        .setValue(userToCreate)
                        .addOnSuccessListener {
                            Timber.d("createUser addOnSuccessListener: $it")
                            continuation.resume(userToCreate)
                        }
                        .addOnFailureListener {
                            continuation.resumeWithException(it)
                        }
                } catch (e: Exception) {
                    Timber.d("createUser: Exception: $e")
                    continuation.resumeWithException(e)
                }
            }
        }

    override suspend fun updateUser(user: UserLocalModel): UserLocalModel =
        withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                val dbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(USERS_DB_KEY)
                    .child(user.user_id)
                try {
                    dbRef
                        .setValue(user)
                        .addOnSuccessListener {
                            Timber.d("updateUser: OnSuccess")
                            continuation.resume(user)
                        }
                        .addOnFailureListener {
                            continuation.resumeWithException(it)
                        }
                } catch (e: Exception) {
                    Timber.d("updateUser: Exception: $e")
                    continuation.resumeWithException(e)
                }
            }
        }

    override suspend fun subscribeToContactsList(userId: String): Flow<ContactsUpdateResult> =
        withContext(ioDispatcher) {
            callbackFlow {
                val dbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(USERS_DB_KEY)
                    .child(userId)
                    .child(USER_FRIENDS_DB_KEY)

                val listener = object : ChildEventListener {
                    override fun onChildAdded(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val friend = snapshot.getValue<FriendLocalModel>() ?: FriendLocalModel()
                        Timber.d("onChildAdded $snapshot $friend")
                        trySend(ContactsUpdateResult.OnAdded(friend))
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val friend = snapshot.getValue<FriendLocalModel>() ?: FriendLocalModel()
                        Timber.d("onChildRemoved $snapshot $friend")
                        trySend(ContactsUpdateResult.OnRemoved(friend))
                    }

                    override fun onChildMoved(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val chat = snapshot.getValue<FriendLocalModel>() ?: FriendLocalModel()
                        Timber.d("onChildMoved $snapshot $chat")
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val friend = snapshot.getValue<FriendLocalModel>() ?: FriendLocalModel()
                        Timber.d("onChildChanged $snapshot $friend")
                        trySend(ContactsUpdateResult.OnUpdated(friend))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.d("subscribeToContactsList onCancelled $error")
                        close(CancellationException("API Error", error.toException()))
                    }
                }

                Timber.d("subscribeToContactsList addChildEventListener")
                dbRef.addChildEventListener(listener)

                awaitClose {
                    Timber.d("subscribeToContactsList awaitClose")
                    dbRef.removeEventListener(listener)
                }
            }
        }

    override suspend fun getFcmToken(userId: String): String {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine<String> { continuation ->
                try {
                    firebaseMessaging.token.addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            Timber.d("token: ${result.result}")
                            continuation.resume(result.result)
                        } else {
                            Timber.d("token getting false")
                            continuation.resume("")
                        }
                    }

                    continuation.invokeOnCancellation {
                        Timber.d("invokeOnCancellation:")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.d("Exception: $e")
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun updateLastSeenTime(userId: String): Long {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                try {
                    databaseReference
                        .child(CURRENT_NAMESPACE)
                        .child(USERS_DB_KEY)
                        .child(userId)
                        .child(USER_LAST_SEEN_DB_KEY)
                        .setValue(System.currentTimeMillis())
                        .addOnSuccessListener {
                            Timber.d("update last_seen: $it")
                            continuation.resume(System.currentTimeMillis())
                        }
                        .addOnFailureListener {
                            Timber.d("update fail: $it")
                            continuation.resumeWithException(it)
                        }

                    continuation.invokeOnCancellation {
                        Timber.d("invokeOnCancellation:")
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.d("Exception: $e")
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun updateFcmToken(userId: String, token: String?): Boolean {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                try {
                    databaseReference
                        .child(CURRENT_NAMESPACE)
                        .child(FCM_TOKENS_DB_KEY)
                        .child(userId)
                        .setValue(token)
                        .addOnSuccessListener {
                            Timber.d("update token: $it")
                            continuation.resume(true)
                        }
                        .addOnFailureListener {
                            Timber.d("update fail: $it")
                            continuation.resumeWithException(it)
                        }
                } catch (e: Exception) {
                    Timber.d("update fail: $e")
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun deleteUserInDbApi(userId: String): Boolean {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val userForUpdate = UserLocalModel(
                        user_first_name = "User",
                        user_last_name = "deleted",
                        user_id = userId,
                        userDeleted = true,
                        user_last_seen = System.currentTimeMillis()
                    )

                    databaseReference
                        .child(CURRENT_NAMESPACE)
                        .child(USERS_DB_KEY)
                        .child(userId)
                        .setValue(userForUpdate)
                        .addOnCompleteListener { result ->
                            continuation.resume(result.isSuccessful)
                            Timber.d("OnComplete $result")
                        }

                } catch (e: Exception) {
                    e.printStackTrace()
                    continuation.resumeWithException(e)
                }
                continuation.invokeOnCancellation {
                    Timber.d("delete user invokeOnCancellation")
                }
            }
        }
    }

    override suspend fun deleteUserFirebaseAuth(userId: String): Boolean {
        return withContext(ioDispatcher) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    user.delete().await()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    companion object {
        private const val NO_NAME_FIELD = "no name"
        private const val NO_EMAIL_FIELD = "no email"
    }
}

sealed class ContactsUpdateResult {
    data class OnAdded(val friend: FriendLocalModel) : ContactsUpdateResult()
    data class OnRemoved(val friend: FriendLocalModel) : ContactsUpdateResult()
    data class OnUpdated(val friend: FriendLocalModel) : ContactsUpdateResult()

}