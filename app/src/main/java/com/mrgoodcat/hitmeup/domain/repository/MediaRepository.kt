package com.mrgoodcat.hitmeup.domain.repository

import com.mrgoodcat.hitmeup.data.repostory.UploadItemType
import com.mrgoodcat.hitmeup.data.repostory.UploadMediaResult
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun uploadPhotoToApi(fileUri: String, item: UploadItemType): Flow<UploadMediaResult>
    suspend fun removeAllMediaFolderById(item: UploadItemType): Boolean
}