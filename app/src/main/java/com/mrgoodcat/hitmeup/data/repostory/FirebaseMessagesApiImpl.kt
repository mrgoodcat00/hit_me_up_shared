package com.mrgoodcat.hitmeup.data.repostory

import android.content.Context
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CHATS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CURRENT_NAMESPACE
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.MESSAGES_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.MESSAGES_TIMESTAMP_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_CHAT_LAST_MESSAGE_SENDER_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_CHAT_LAST_MESSAGE_TEXT_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.USER_CHAT_LAST_MESSAGE_TIMESTAMP_DB_KEY
import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.model.extensions.getContentToObject
import com.mrgoodcat.hitmeup.domain.model.extensions.getContentType
import com.mrgoodcat.hitmeup.domain.model.extensions.toMessagesModel
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessageDirection
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessageDirection.ASC
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessageDirection.DESC
import com.mrgoodcat.hitmeup.domain.repository.FirebaseMessagesApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseMessagesApiImpl(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val appScope: CoroutineScope
) : FirebaseMessagesApi {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference: DatabaseReference = database.getReferenceFromUrl(
        context.getString(R.string.firebase_database_url_string)
    )

    override suspend fun getMessagesAsList(
        chatId: String,
        pageSize: Int,
        lastItem: Long,
        direction: FirebaseMessageDirection
    ): List<MessageModel> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val resultList = ArrayList<MessageModel>()

                val dbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(MESSAGES_DB_KEY)
                    .child(chatId)

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.map {
                            val messageType = object :
                                GenericTypeIndicator<@JvmSuppressWildcards MessageModel>() {}
                            val mes = it.getValue(messageType) ?: MessageModel()
                            resultList.add(mes)
                        }
                        Timber.d("MessageModel onDataChange: ${resultList.size}")
                        continuation.resume(resultList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.d("MessageModel onCancelled: ${error.message}")
                        resultList.addAll(emptyList())
                        continuation.resumeWithException(Exception(error.message))
                    }
                }

                if (continuation.isCancelled) {
                    dbRef.removeEventListener(listener)
                    continuation.cancel()
                    return@suspendCancellableCoroutine
                }

                when (direction) {
                    ASC -> {
                        dbRef.orderByChild(MESSAGES_TIMESTAMP_DB_KEY)
                            .startAfter(lastItem.toDouble())
                            .limitToFirst(pageSize)
                            .addListenerForSingleValueEvent(listener)
                        Timber.d("add asc listener")
                    }

                    DESC -> {
                        dbRef.orderByChild(MESSAGES_TIMESTAMP_DB_KEY)
                            .endBefore(lastItem.toDouble())
                            .limitToLast(pageSize)
                            .addListenerForSingleValueEvent(listener)
                        Timber.d("add desc listener")
                    }
                }

                continuation.invokeOnCancellation {
                    Timber.d("getMessagesAsList: invokeOnCancellation")
                    Timber.d("remove listener")
                    dbRef.removeEventListener(listener)
                }

            } catch (exception: Exception) {
                Timber.d("remove listener while exception")
                Timber.d("getMessagesAsList exception: ${exception.message}")
                continuation.resumeWithException(exception)
            }
        }
    }

    override suspend fun subscribeOnChatMessages(chatId: String): Flow<MessageModel> {
        val dbRef = databaseReference
            .child(CURRENT_NAMESPACE)
            .child(MESSAGES_DB_KEY)
            .child(chatId)
            .orderByChild(MESSAGES_TIMESTAMP_DB_KEY)
            .limitToLast(1)
        return withContext(ioDispatcher) {
            callbackFlow {
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            val chatType = object :
                                GenericTypeIndicator<@JvmSuppressWildcards MessageLocalModel>() {}
                            val message = it.getValue(chatType) ?: MessageLocalModel()
                            Timber.d(
                                "getContentToObject:${
                                    message.toMessagesModel().getContentToObject()
                                }"
                            )
                            trySend(message.toMessagesModel())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Timber.d("subscribeOnChatMessages onCancelled")
                        close(CancellationException("API Error", error.toException()))
                    }
                }

                dbRef.addValueEventListener(listener)

                awaitClose {
                    Timber.d("subscribeOnChatMessages removeEventListener")
                    dbRef.removeEventListener(listener)
                }
            }
        }
    }

    override suspend fun sendMessage(
        message: MessageModel,
        currentChatId: String,
    ): Result<Boolean> {
        return appScope.async {
            suspendCancellableCoroutine { continuation ->

                val sendMessageDbRef = databaseReference
                    .child(CURRENT_NAMESPACE)
                    .child(MESSAGES_DB_KEY)
                    .child(currentChatId)
                    .child(message.id)
                    .setValue(message)

                val successListener =
                    OnSuccessListener<Void> { continuation.resume(Result.success(true)) }
                val errorListener = OnFailureListener { continuation.resumeWithException(it) }

                val lastMessageContent = message.getContentType()

                try {
                    sendMessageDbRef
                        .addOnSuccessListener {
                            val childUpdates = hashMapOf<String, Any>(
                                "/$CURRENT_NAMESPACE/$CHATS_DB_KEY/$currentChatId/$USER_CHAT_LAST_MESSAGE_TIMESTAMP_DB_KEY" to message.timestamp,
                                "/$CURRENT_NAMESPACE/$CHATS_DB_KEY/$currentChatId/$USER_CHAT_LAST_MESSAGE_TEXT_DB_KEY" to lastMessageContent,
                                "/$CURRENT_NAMESPACE/$CHATS_DB_KEY/$currentChatId/$USER_CHAT_LAST_MESSAGE_SENDER_DB_KEY" to message.sender
                            )

                            databaseReference
                                .updateChildren(childUpdates)
                                .addOnSuccessListener(successListener)
                                .addOnFailureListener(errorListener)
                        }
                        .addOnFailureListener(errorListener)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }.await()
    }
}