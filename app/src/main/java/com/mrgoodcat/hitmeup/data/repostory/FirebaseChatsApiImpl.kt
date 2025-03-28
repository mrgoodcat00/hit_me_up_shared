package com.mrgoodcat.hitmeup.data.repostory

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.data.model.ChatThinLocalModel
import com.mrgoodcat.hitmeup.data.model.UserThinLocalModel
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CHATS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CURRENT_NAMESPACE
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.MESSAGES_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USERS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_CHATS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_CHAT_ID_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_CHAT_LAST_MESSAGE_TIMESTAMP_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_CHAT_PARTICIPANTS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_FRIENDS_DB_KEY
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.repository.FirebaseChatsApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseChatsApiImpl(
    @ApplicationContext private val context: Context,
    private val ioCoroutineScope: CoroutineDispatcher
) : FirebaseChatsApi {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var databaseReference: DatabaseReference = database.getReferenceFromUrl(
        context.getString(R.string.firebase_database_url_string)
    )

    private fun fetchChatDataById(chatId: String, callback: (ChatLocalModel) -> Unit) {
        val singleEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatType =
                    object : GenericTypeIndicator<@JvmSuppressWildcards ChatLocalModel>() {}
                val chat = snapshot.getValue(chatType) ?: ChatLocalModel()
                callback(chat)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(ChatLocalModel())
            }
        }

        val dbRef = databaseReference
            .child(CURRENT_NAMESPACE)
            .child(CHATS_DB_KEY)
            .child(chatId)

        try {
            dbRef.addListenerForSingleValueEvent(singleEventListener)
        } catch (exception: Exception) {
            Timber.d("exception: invokeOnCancellation ${exception.message}")
            dbRef.removeEventListener(singleEventListener)
            callback(ChatLocalModel())
        }
    }

    override suspend fun getChatsAsList(pageSize: Int, lastItem: String): List<ChatLocalModel> {
        return withContext(ioCoroutineScope) {
            suspendCancellableCoroutine { continuation ->

                val resultList = ArrayList<ChatLocalModel>()
                var resultSize: Int

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        resultSize = snapshot.children.count()

                        if (resultSize == 0) {
                            continuation.resume(resultList)
                        }

                        snapshot.children.map {
                            val chatType = object :
                                GenericTypeIndicator<@JvmSuppressWildcards ChatLocalModel>() {}
                            val chat = it.getValue(chatType) ?: ChatLocalModel()

                            fetchChatDataById(chat.chat_id) { chatModel ->
                                resultList.add(chatModel)

                                if (resultList.size == resultSize) {
                                    continuation.resume(resultList)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.d("onCancelled: ${error.message}")
                        resultList.addAll(emptyList())
                        continuation.resumeWithException(Exception())
                    }
                }

                val dbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(USERS_DB_KEY)
                    .child(firebaseAuth.currentUser!!.uid)
                    .child(USER_CHATS_DB_KEY)
                try {

                    dbRef
                        .orderByChild(USER_CHAT_ID_DB_KEY)
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

    override suspend fun getChatById(id: String): ChatLocalModel? {
        return withContext(ioCoroutineScope) {
            suspendCancellableCoroutine { continuation ->
                try {
                    fetchChatDataById(id) {
                        continuation.resume(it)
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun deleteChatById(chat: ChatModel, myId: String): Boolean {
        return withContext(ioCoroutineScope) {
            suspendCancellableCoroutine { continuation ->
                try {
                    if (chat.title.lowercase() != "one to one") {
                        continuation.resumeWithException(Exception("Unknown chat type ${chat.id}"))
                        return@suspendCancellableCoroutine
                    }

                    val dbRef = databaseReference.child(CURRENT_NAMESPACE)

                    val chatId = chat.id
                    val collocutorId = chat.participantIds.keys.find { it != myId }

                    val childUpdates = hashMapOf<String, Any?>(
                        "/$USERS_DB_KEY/$myId/$USER_FRIENDS_DB_KEY/$collocutorId" to null,
                        "/$USERS_DB_KEY/$myId/$USER_CHATS_DB_KEY/$chatId" to null,
                        "/$USERS_DB_KEY/$collocutorId/$USER_CHATS_DB_KEY/$chatId" to null,
                        "/$USERS_DB_KEY/$collocutorId/$USER_FRIENDS_DB_KEY/$myId" to null,
                        "/$CHATS_DB_KEY/$chatId" to null,
                        "/$MESSAGES_DB_KEY/$chatId" to null,
                    )

                    Timber.d("deleteChat: $childUpdates")

                    dbRef
                        .updateChildren(childUpdates)
                        .addOnSuccessListener {
                            continuation.resume(true)
                        }
                        .addOnFailureListener {
                            continuation.resumeWithException(it)
                        }

                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun subscribeOnChatUpdates(myId: String): Flow<ChatUpdateResult> =
        withContext(ioCoroutineScope) {
            callbackFlow {
                val dbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(CHATS_DB_KEY)
                    .orderByChild("$USER_CHAT_PARTICIPANTS_DB_KEY/$myId")
                    .equalTo(true)

                val listener = object : ChildEventListener {
                    override fun onChildAdded(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val chat = snapshot.getValue<ChatLocalModel>() ?: ChatLocalModel()
                        Timber.d("onChildAdded $snapshot $chat")
                        trySend(ChatUpdateResult.OnAdded(chat))
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val chat = snapshot.getValue<ChatLocalModel>() ?: ChatLocalModel()
                        Timber.d("onChildChanged $snapshot $chat")
                        trySend(ChatUpdateResult.OnChanged(chat))
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val chat = snapshot.getValue<ChatLocalModel>() ?: ChatLocalModel()
                        Timber.d("onChildRemoved $snapshot $chat")
                        trySend(ChatUpdateResult.OnRemoved(chat))
                    }

                    override fun onChildMoved(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val chat = snapshot.getValue<ChatLocalModel>() ?: ChatLocalModel()
                        Timber.d("onChildMoved $snapshot $chat")
                        trySend(ChatUpdateResult.OnMoved(chat))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.d("subscribeOnChatMessages onCancelled $error")
                        close(CancellationException("API Error", error.toException()))
                    }
                }

                Timber.d("subscribeOnChatMessages addChildEventListener")
                dbRef.addChildEventListener(listener)

                awaitClose {
                    Timber.d("subscribeOnChatMessages removeEventListener")
                    dbRef.removeEventListener(listener)
                }
            }
        }

    override suspend fun createChatWithUser(
        ownerId: String,
        collocutorId: String
    ): Result<ChatLocalModel> {
        return withContext(ioCoroutineScope) {
            suspendCancellableCoroutine { continuation ->
                val createChatDbReference = databaseReference.child(CURRENT_NAMESPACE)

                try {
                    createChatDbReference
                        .child(CHATS_DB_KEY)
                        .child(ownerId + "_" + collocutorId)
                        .get()
                        .addOnSuccessListener { ownerCollocutorChat ->
                            if (!ownerCollocutorChat.exists()) {
                                createChatDbReference
                                    .child(CHATS_DB_KEY)
                                    .child(collocutorId + "_" + ownerId)
                                    .get()
                                    .addOnSuccessListener { collocutorOwnerChat ->
                                        if (!collocutorOwnerChat.exists()) {

                                            val participantIds = HashMap<String, Boolean>()
                                            participantIds[ownerId] = true
                                            participantIds[collocutorId] = true

                                            val newChatModel = ChatLocalModel(
                                                chat_id = ownerId + "_" + collocutorId,
                                                owner = ownerId,
                                                title = "One to one",
                                                participant_ids = participantIds,
                                                last_message_timestamp = System.currentTimeMillis(),
                                            )

                                            val newEmptyChatModel =
                                                ChatThinLocalModel(ownerId + "_" + collocutorId)

                                            val newEmptyMeModel = UserThinLocalModel(ownerId)

                                            val newEmptyCollocutorModel =
                                                UserThinLocalModel(collocutorId)

                                            val childUpdates = hashMapOf<String, Any>(
                                                "/$USERS_DB_KEY/$ownerId/$USER_FRIENDS_DB_KEY/${newEmptyCollocutorModel.user_id}" to newEmptyCollocutorModel.getMapToCreateChat(),
                                                "/$USERS_DB_KEY/$ownerId/$USER_CHATS_DB_KEY/${newChatModel.chat_id}" to newEmptyChatModel.getMapToCreateChat(),
                                                "/$USERS_DB_KEY/$collocutorId/$USER_CHATS_DB_KEY/${newChatModel.chat_id}" to newEmptyChatModel.getMapToCreateChat(),
                                                "/$USERS_DB_KEY/$collocutorId/$USER_FRIENDS_DB_KEY/${newEmptyMeModel.user_id}" to newEmptyMeModel.getMapToCreateChat(),
                                                "/$CHATS_DB_KEY/${newChatModel.chat_id}" to newChatModel.getMapToCreateChat()
                                            )

                                            Timber.d("createChatWithUser: $childUpdates")

                                            createChatDbReference
                                                .updateChildren(childUpdates)
                                                .addOnSuccessListener {
                                                    continuation.resume(Result.success(newChatModel))
                                                }
                                                .addOnFailureListener {
                                                    continuation.resumeWithException(it)
                                                }

                                        } else {
                                            val chat =
                                                collocutorOwnerChat.getValue<ChatLocalModel>()
                                                    ?: ChatLocalModel()
                                            continuation.resume(Result.success(chat))
                                        }
                                    }
                            } else {
                                val chat = ownerCollocutorChat.getValue<ChatLocalModel>()
                                if (chat == null) {
                                    continuation.resumeWithException(Exception(context.getString(R.string.collocutor_excpetion)))
                                } else {
                                    continuation.resume(Result.success(chat))
                                }
                            }
                        }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    @Deprecated("Old way to get chats")
    suspend fun getChatsAsLists(
        pageSize: Int,
        lastItem: Long,
    ): List<ChatLocalModel> {
        return withContext(ioCoroutineScope) {
            suspendCancellableCoroutine { continuation ->
                val resultList = ArrayList<ChatLocalModel>()

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.map {
                            Timber.d("onDataChange: $it")
                            val chatType = object :
                                GenericTypeIndicator<@JvmSuppressWildcards ChatLocalModel>() {}
                            val mes = it.getValue(chatType) ?: ChatLocalModel()
                            resultList.add(mes)
                        }

                        continuation.resume(resultList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.d("onDataChange: ${error.message}")
                        resultList.addAll(emptyList())
                        continuation.resumeWithException(Exception())
                    }
                }

                val dbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(CHATS_DB_KEY)

                try {

                    dbRef
                        .orderByChild(USER_CHAT_LAST_MESSAGE_TIMESTAMP_DB_KEY)
                        .startAfter(lastItem.toDouble())
                        .limitToFirst(pageSize)
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
}

sealed class ChatUpdateResult {
    data class OnChanged(val chat: ChatLocalModel) : ChatUpdateResult()
    data class OnAdded(val chat: ChatLocalModel) : ChatUpdateResult()
    data class OnRemoved(val chat: ChatLocalModel) : ChatUpdateResult()
    data class OnMoved(val chat: ChatLocalModel) : ChatUpdateResult()
}