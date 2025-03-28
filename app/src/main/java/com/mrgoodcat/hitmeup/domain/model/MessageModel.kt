package com.mrgoodcat.hitmeup.domain.model

import androidx.annotation.Keep

@Keep
data class MessageModel(
    val id: String = "",
    val sender: String = "",
    val text: String = "",
    val content: Map<String, String> = emptyMap(),
    val timestamp: Long = 0,
    val chat_id: String = "",
) {
    constructor() : this("", "", "", emptyMap(), 0, "")
}