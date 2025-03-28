package com.mrgoodcat.hitmeup.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ChatThinLocalModel(
    val chat_id: String = "",
) {
    @Exclude
    fun getMapToCreateChat(): Map<String, Any?> {
        return mapOf(
            "chat_id" to chat_id,
        )
    }
}
