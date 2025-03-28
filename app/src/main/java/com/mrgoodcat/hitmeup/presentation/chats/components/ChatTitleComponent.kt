package com.mrgoodcat.hitmeup.presentation.chats.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Composable
fun ChatTitleComponent(collocutors: List<UserModel>, modifier: Modifier) {
    val chatTitle = StringBuilder("")

    collocutors.mapIndexed { index, element ->
        chatTitle.append("${element.user_first_name} ${element.user_last_name}")

        if (index != collocutors.size - 1)
            chatTitle.append(", ")
    }

    Text(
        modifier = modifier,
        text = chatTitle.toString(),
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        color = BlackTitle,
        lineHeight = 24.sp,
        fontSize = 16.sp,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}