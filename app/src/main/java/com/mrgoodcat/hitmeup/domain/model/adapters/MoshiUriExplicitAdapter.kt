package com.mrgoodcat.hitmeup.domain.model.adapters

import android.net.Uri
import androidx.core.net.toUri
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class MoshiUriExplicitAdapter {
    @ToJson
    fun toJson(uri: Uri): String {
        return uri.toString()
    }

    @FromJson
    fun fromJson(uri: String): Uri {
        return uri.toUri()
    }
}