package com.mrgoodcat.hitmeup.domain.usecase.messages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import com.mrgoodcat.hitmeup.data.push_notification.PushNotificationBuilder
import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.model.Constants
import com.mrgoodcat.hitmeup.domain.model.FirebasePushMessageModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

open class CreateNotificationUseCase @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dbRepository: DbRepository,
    private val pushNotificationBuilder: PushNotificationBuilder,
) {
    suspend fun execute(
        pushModel: FirebasePushMessageModel
    ) {
        CoroutineScope(ioDispatcher).launch {
            val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (!isGranted || dbRepository.getAppSettings()
                    ?.currentOpenedChatId == pushModel.chatId || dbRepository.getAppSettings()
                    ?.getCurrentScreenName() == Constants.HitMeUpScreen.ChatsScreen
            ) {
                return@launch
            }

            val notificationBuilder =
                pushNotificationBuilder.buildNotificationWithNewMessage(
                    applicationContext, pushModel
                )

            val imageUrl = pushModel.pushInternalObject?.imageUrl

            try {
                if (imageUrl.toString().isNotEmpty()) {
                    val bit = async {
                        try {
                            val loader = ImageLoader(applicationContext)
                            val request = ImageRequest.Builder(applicationContext)
                                .data(imageUrl)
                                .allowHardware(true)
                                .build()

                            when (val result = loader.execute(request)) {
                                is ErrorResult -> {
                                    null
                                }

                                is SuccessResult -> {
                                    (result.image.asDrawable(applicationContext.resources)
                                            as BitmapDrawable).bitmap
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }.await()
                    notificationBuilder.setLargeIcon(bit)
                }

                NotificationManagerCompat
                    .from(applicationContext)
                    .notify(
                        pushModel.pushInternalObject?.tag,
                        1000,
                        notificationBuilder.build()
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}