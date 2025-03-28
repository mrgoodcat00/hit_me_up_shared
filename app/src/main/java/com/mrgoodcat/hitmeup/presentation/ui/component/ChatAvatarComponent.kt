package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.UserModel


@Composable
fun ChatAvatarComponent(
    collocutor: List<UserModel>, currentChat: ChatModel, iconSize: Size
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(iconSize.width.dp)
            .height(iconSize.height.dp)
            .clip(CircleShape)
            .background(
                Brush.sweepGradient(
                    listOf(Color.Green, Color.Yellow),
                    Offset.Infinite
                ), shape = CircleShape, 0.6F
            )
    ) {
        if (currentChat.chatAvatar.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentChat.chatAvatar)
                    .crossfade(true)
                    .error(R.drawable.fui_ic_anonymous_white_24dp)
                    .build(),
                placeholder = painterResource(id = R.drawable.fui_ic_anonymous_white_24dp),
                contentDescription = currentChat.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(iconSize.width.dp)
                    .height(iconSize.height.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(Color.LightGray, Color.DarkGray),
                            Offset.Infinite
                        ), shape = CircleShape, 0.5F
                    )
            )
        } else if (collocutor.size > 1) {
            Text(
                text = currentChat.title.substring(0, 1),
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (collocutor.isEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("")
                    .crossfade(true)
                    .error(R.drawable.fui_ic_anonymous_white_24dp)
                    .build(),
                placeholder = painterResource(id = R.drawable.fui_ic_anonymous_white_24dp),
                contentDescription = currentChat.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(iconSize.width.dp)
                    .height(iconSize.height.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(Color.LightGray, Color.DarkGray),
                            Offset.Infinite
                        ), shape = CircleShape, 0.5F
                    )
            )
        } else if (collocutor[0].user_avatar.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(collocutor[0].user_avatar)
                    .crossfade(true)
                    .error(R.drawable.fui_ic_anonymous_white_24dp)
                    .build(),
                placeholder = painterResource(id = R.drawable.fui_ic_anonymous_white_24dp),
                contentDescription = currentChat.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(iconSize.width.dp)
                    .height(iconSize.height.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(Color.LightGray, Color.DarkGray),
                            Offset.Infinite
                        ), shape = CircleShape, 0.5F
                    )
            )
        } else {
            val fName =
                if (collocutor.first().user_first_name.length > 1) collocutor.first().user_first_name.substring(
                    0,
                    1
                ) else ""
            val lName =
                if (collocutor.first().user_last_name.length > 1) collocutor.first().user_last_name.substring(
                    0,
                    1
                ) else ""
            Text(
                text = "$fName$lName".toUpperCase(Locale.current),
                textAlign = TextAlign.Center,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}