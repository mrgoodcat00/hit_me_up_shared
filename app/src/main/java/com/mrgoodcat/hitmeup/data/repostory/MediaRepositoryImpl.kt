package com.mrgoodcat.hitmeup.data.repostory

import android.content.Context
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.CURRENT_NAMESPACE
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.MEDIA_CHATS_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.MEDIA_MESSAGES_DB_KEY
import com.mrgoodcat.hitmeup.data.repostory.Constants.Companion.MEDIA_USERS_DB_KEY
import com.mrgoodcat.hitmeup.domain.repository.MediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val context: Context,
    private val appScope: CoroutineScope
) :
    MediaRepository {
    private val storage = Firebase.storage.reference

    override suspend fun uploadPhotoToApi(
        fileUri: String,
        item: UploadItemType
    ): Flow<UploadMediaResult> {
        return appScope.async {
            val file = fileUri.toUri()
            callbackFlow {
                try {
                    val pathToUpload = when (item) {
                        is UploadItemType.Chats -> "$CURRENT_NAMESPACE/$MEDIA_CHATS_DB_KEY/${item.chatId}/${file.lastPathSegment}"
                        is UploadItemType.Messages -> "$CURRENT_NAMESPACE/$MEDIA_MESSAGES_DB_KEY/${item.chatId}/${file.lastPathSegment}"
                        is UploadItemType.Users -> "$CURRENT_NAMESPACE/$MEDIA_USERS_DB_KEY/${item.userId}/${file.lastPathSegment}"
                    }

                    val riversRef = storage.child(pathToUpload)

                    val uploadTask = riversRef.putFile(file)

                    val fail = OnFailureListener {
                        Timber.d("file fail: ${it.message}")
                        trySend(
                            UploadMediaResult.Error(
                                it.message ?: context.getString(
                                    R.string.failed_to_upload_media,
                                    file
                                )
                            )
                        )
                    }
                    val progress = OnProgressListener<UploadTask.TaskSnapshot> { progress ->
                        Timber.d("prog: ${progress.bytesTransferred} from ${progress.totalByteCount} ")
                        trySend(
                            UploadMediaResult.Loading(
                                progress.bytesTransferred,
                                progress.totalByteCount
                            )
                        )
                    }
                    val success = OnSuccessListener<UploadTask.TaskSnapshot> {
                        riversRef.downloadUrl.addOnSuccessListener {
                            Timber.d("file: $it")
                            trySend(UploadMediaResult.Success(it.toString()))
                        }
                    }

                    uploadTask
                        .addOnProgressListener(progress)
                        .addOnFailureListener(fail)
                        .addOnSuccessListener(success)

                    awaitClose {
                        Timber.d("awaitClose")
                        uploadTask.cancel()
                        uploadTask.removeOnFailureListener(fail)
                        uploadTask.removeOnProgressListener(progress)
                        uploadTask.removeOnSuccessListener(success)
                    }
                } catch (e: Exception) {
                    close(e)
                }
            }
        }.await()
    }

    override suspend fun removeAllMediaFolderById(item: UploadItemType): Boolean {
        return appScope.async {
            try {
                val pathToErase = when (item) {
                    is UploadItemType.Chats -> "$CURRENT_NAMESPACE/$MEDIA_CHATS_DB_KEY/${item.chatId}/"
                    is UploadItemType.Messages -> "$CURRENT_NAMESPACE/$MEDIA_MESSAGES_DB_KEY/${item.chatId}/"
                    is UploadItemType.Users -> "$CURRENT_NAMESPACE/$MEDIA_USERS_DB_KEY/${item.userId}/"
                }

                val listRef = storage.child(pathToErase)

                listRef
                    .listAll()
                    .addOnSuccessListener { result ->
                        for (mediaItem in result.items) {
                            Timber.d("item $mediaItem")
                            mediaItem.delete()
                        }
                    }
                    .addOnFailureListener {
                        Timber.d("FailureListener $it")
                    }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d("Exception")
                false
            }
        }.await()
    }

}

sealed class UploadItemType {
    data class Messages(val chatId: String) : UploadItemType()
    data class Users(val userId: String) : UploadItemType()
    data class Chats(val chatId: String) : UploadItemType()
}

sealed class UploadMediaResult {
    data class Loading(val transferred: Long, val total: Long) : UploadMediaResult()
    data class Error(val message: String) : UploadMediaResult()
    data class Success(val url: String) : UploadMediaResult()
}