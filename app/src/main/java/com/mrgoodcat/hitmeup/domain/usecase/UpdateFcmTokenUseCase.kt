package com.mrgoodcat.hitmeup.domain.usecase

import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

class UpdateFcmTokenUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val firebaseUsersApi: FirebaseUsersApi,
) {
    suspend fun execute(id: String, erase: Boolean = false): Any? {
        return CoroutineScope(ioDispatcher).async {
            suspendCancellableCoroutine { continuation ->
                try {
                    launch {
                        val token = firebaseUsersApi.getFcmToken(id)
                        firebaseUsersApi.updateFcmToken(id, if (erase) null else token)
                    }
                    continuation.resumeWith(Result.success(null))
                } catch (e: Exception) {
                    e.printStackTrace()
                    continuation.resumeWithException(e)
                }

                continuation.invokeOnCancellation {
                    Timber.d("UpdateFcmTokenUseCase awaitClose")
                }
            }
        }.await()
    }
}