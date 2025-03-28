package com.mrgoodcat.hitmeup.domain.model


sealed class MessageContentType {
    data class SimpleText(
        val text: String = "",
        val type: String = MESSAGE_CONTENT_TYPE_SIMPLE_TEXT
    ) : MessageContentType()

    data class TextWithImage(
        val text: String = "",
        val image: String = "",
        val type: String = MESSAGE_CONTENT_TYPE_TEXT_WITH_IMAGE
    ) : MessageContentType()

    data class Image(
        val image: String = "",
        val type: String = MESSAGE_CONTENT_TYPE_IMAGE
    ) : MessageContentType()

    companion object {
        const val MESSAGE_CONTENT_TYPE_IMAGE = "image"
        const val MESSAGE_CONTENT_TYPE_TEXT_WITH_IMAGE = "textImage"
        const val MESSAGE_CONTENT_TYPE_SIMPLE_TEXT = "simpleText"
    }
}

