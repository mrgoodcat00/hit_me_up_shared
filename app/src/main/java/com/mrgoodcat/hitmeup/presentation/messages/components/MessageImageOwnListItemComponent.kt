package com.mrgoodcat.hitmeup.presentation.messages.components

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.MessageContentType
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.model.extensions.getContentToObject
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightGreyBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto
import java.text.SimpleDateFormat
import java.util.Date

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MessageImageOwnComponent(
    message: MessageModel = MessageModel(),
    previousWasMe: Boolean = false,
    nextMessageWillMine: Boolean = false,
    onImageClick: (String) -> Unit = {},
    dataHasChanged: Long = 0L
) {
    val format = SimpleDateFormat(stringResource(id = R.string.time_format_HH_mm))
    val messageTime = format.format(Date(message.timestamp))

    val messageTopPadding: Dp = if (nextMessageWillMine) {
        3.dp
    } else {
        15.dp
    }

    if (dataHasChanged != 0L) {
        MessageImageOwnHasTitleWrapper(
            dataHasChanged,
            Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = CenterEnd
            ) {
                messageContent(
                    messageTime = messageTime,
                    message = message,
                    onImageClick = onImageClick
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = messageTopPadding),
            contentAlignment = CenterEnd
        ) {
            messageContent(
                messageTime = messageTime,
                message = message,
                onImageClick = onImageClick
            )
        }
    }
}

@Composable
fun messageContent(
    messageTime: String,
    message: MessageModel,
    onImageClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(0.85f),
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
        ) {
            Box(
                modifier = Modifier,
            ) {
                val uri = when (val content = message.getContentToObject()) {
                    is MessageContentType.Image -> {
                        content.image
                    }

                    is MessageContentType.SimpleText -> {
                        ""
                    }

                    is MessageContentType.TextWithImage -> {
                        ""
                    }
                }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .error(R.drawable.ic_add_image)
                        .build(),
                    placeholder = painterResource(id = R.drawable.fui_ic_anonymous_white_24dp),
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .border(0.5.dp, LightGreyBorder, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onImageClick(uri)
                        }
                )
                Text(
                    style = MaterialTheme.typography.headlineLarge.copy(
                        shadow = Shadow(
                            color = Color.Gray,
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    ),
                    lineHeight = 14.sp,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    text = messageTime,
                    color = Color.White,
                    modifier = Modifier
                        .align(BottomEnd)
                        .offset(
                            x = 2.dp,
                            y = 2.dp
                        )
                        .alpha(1f)
                        .padding(end = 10.dp, bottom = 10.dp)
                )
            }
        }
    }
}

@Composable
fun MessageImageOwnHasTitleWrapper(
    dataHasChanged: Long,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val formatForTitle = SimpleDateFormat(stringResource(id = R.string.date_format_dd_MMMM_yyyy))
    val messageTitle = if (DateUtils.isToday(dataHasChanged)) {
        stringResource(id = R.string.message_title_today)
    } else {
        formatForTitle.format(Date(dataHasChanged))
    }

    Column {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = messageTitle,
            letterSpacing = 0.sp,
            fontFamily = Roboto,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            color = GrayTitle,
            modifier = modifier,
            textAlign = TextAlign.Center
        )
        Spacer(
            modifier = Modifier
                .height(15.dp)
                .fillMaxWidth()
        )

        content.invoke()
    }
}