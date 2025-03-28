package com.mrgoodcat.hitmeup.domain.usecase.edit_profile

import android.net.Uri
import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel
import com.mrgoodcat.hitmeup.data.repostory.UploadItemType
import com.mrgoodcat.hitmeup.data.repostory.UploadMediaResult
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class UploadImageUseCase @Inject constructor(
    private val dbRepository: DbRepository,
    private val mediaRepository: MediaRepository,
) {
    suspend fun execute(uri: Uri): Flow<UploadMediaResult> {
        val currentUserProfile = dbRepository.getUserProfile() ?: UserProfileLocalModel()

        mediaRepository.removeAllMediaFolderById(UploadItemType.Users(currentUserProfile.user_id))

        return mediaRepository.uploadPhotoToApi(
            uri.toString(),
            UploadItemType.Users(currentUserProfile.user_id)
        )
    }

}