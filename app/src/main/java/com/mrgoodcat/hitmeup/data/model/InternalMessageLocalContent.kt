package com.mrgoodcat.hitmeup.data.model


sealed class InternalMessageLocalContent {
    data class SimpleText(
        val text: String = "",
        val type: String = "simpleText"
    ) : InternalMessageLocalContent()

    data class TextWithImage(
        val text: String = "",
        val image: String = "",
        val type: String = "textImage"
    ) : InternalMessageLocalContent()

    data class Image(
        val image: String = "",
        val type: String = "image"
    ) : InternalMessageLocalContent()
}

