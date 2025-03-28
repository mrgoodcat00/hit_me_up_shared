package com.mrgoodcat.hitmeup.domain.usecase.messages

import android.net.Uri
import com.mrgoodcat.hitmeup.data.repostory.UploadItemType
import com.mrgoodcat.hitmeup.data.repostory.UploadMediaResult
import com.mrgoodcat.hitmeup.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class UploadImageUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend fun execute(uri: Uri, chatId: String): Flow<UploadMediaResult> {
        return mediaRepository.uploadPhotoToApi(
            uri.toString(),
            UploadItemType.Messages(chatId)
        )
    }

}