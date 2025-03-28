package com.mrgoodcat.hitmeup.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserThinLocalModel(
    val user_id: String = "",
) {
    @Exclude
    fun getMapToCreateChat(): Map<String, Any?> {
        return mapOf(
            "user_id" to user_id,
        )
    }
}
