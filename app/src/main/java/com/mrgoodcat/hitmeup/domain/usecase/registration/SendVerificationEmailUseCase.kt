package com.mrgoodcat.hitmeup.domain.usecase.registration

import com.google.firebase.auth.FirebaseAuth
import com.mrgoodcat.hitmeup.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject

class SendVerificationEmailUseCase @Inject constructor(
    @IoDispatcher val ioDispatcher: CoroutineDispatcher
) {
    suspend fun execute(): Flow<Boolean> {
        return CoroutineScope(ioDispatcher).async {
            callbackFlow {
                try {
                    Timber.d("SendVerificationEmailUseCase isSuccessful:${FirebaseAuth.getInstance().currentUser}")

                    FirebaseAuth
                        .getInstance()
                        .currentUser
                        ?.sendEmailVerification()
                        ?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                trySend(true)
                                Timber.d("SendVerificationEmailUseCase isSuccessful:${it.result}")
                            } else {
                                trySend(false)
                                Timber.d("SendVerificationEmailUseCase isFail:${it.exception}")
                            }
                        }
                } catch (e: Exception) {
                    Timber.d("SendVerificationEmailUseCase fail:${e.message}")
                    e.printStackTrace()
                }

                awaitClose {
                    Timber.d("SendVerificationEmailUseCase awaitClose")
                }
            }
        }.await()
    }
}

