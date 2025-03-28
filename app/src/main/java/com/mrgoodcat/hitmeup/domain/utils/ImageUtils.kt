package com.mrgoodcat.hitmeup.domain.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import java.io.File

class ImageUtils {
    companion object {
        fun createTempFile(context: Context): Uri {
            val storage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("image_${System.currentTimeMillis()}", ".jpg", storage)
            return FileProvider.getUriForFile(context, "com.mrgoodcat.hitmeup.provider", file)
        }

        fun deleteTempFile(uri: Uri?, context: Context): Int {
            if (uri == null || uri.path == null) {
                return -1
            }

            return try {
                if (DocumentsContract.deleteDocument(context.contentResolver, uri)) 1 else 0
            } catch (e: Exception) {
                -1
            }
        }
    }
}