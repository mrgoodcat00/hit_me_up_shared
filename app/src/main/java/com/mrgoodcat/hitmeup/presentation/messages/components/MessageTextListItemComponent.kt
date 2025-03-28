package com.mrgoodcat.hitmeup.presentation.messages.components

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.presentation.messages.operateMessageText
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyBottomBarBackground
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto
import java.text.SimpleDateFormat
import java.util.Date

@Preview(backgroundColor = 0xFFFFFFFF)
@Composable
fun MessageTextComponent(
    messageModel: MessageModel = MessageModel(),
    messageText: String = "Hello my friend! Hello my friend!",
    user: UserModel = UserModel(),
    previousWasMe: Boolean = false,
    nextMessageWillMine: Boolean = false,
    dataHasChanged: Long = 0L
) {

    val format = SimpleDateFormat(stringResource(id = R.string.time_format_HH_mm))
    val messageTime = format.format(Date(messageModel.timestamp))

    val messageTopPadding: Dp = if (nextMessageWillMine) {
        3.dp
    } else {
        15.dp
    }

    val leftBottomBorderRadius: Float =
        if ((!previousWasMe && nextMessageWillMine) || (!previousWasMe && !nextMessageWillMine)) {
            5F
        } else {
            30f
        }

    if (dataHasChanged != 0L) {
        MessageHasTitleWrapper(
            dataHasChanged = dataHasChanged,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = CenterStart
            ) {
                Content(
                    previousWasMe = previousWasMe,
                    nextMessageWillMine = nextMessageWillMine,
                    user = user,
                    leftBottomBorderRadius = leftBottomBorderRadius,
                    messageText = messageText,
                    messageTime = messageTime
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = messageTopPadding),
            contentAlignment = CenterStart
        ) {
            Content(
                previousWasMe = previousWasMe,
                nextMessageWillMine = nextMessageWillMine,
                user = user,
                leftBottomBorderRadius = leftBottomBorderRadius,
                messageText = messageText,
                messageTime = messageTime
            )
        }
    }
}

@Composable
fun Content(
    previousWasMe: Boolean,
    nextMessageWillMine: Boolean,
    user: UserModel,
    leftBottomBorderRadius: Float,
    messageText: String,
    messageTime: String
) {
    var textOverflow by remember {
        mutableStateOf(false)
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        if ((!previousWasMe && nextMessageWillMine) || (!previousWasMe && !nextMessageWillMine)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.user_avatar)
                    .crossfade(true)
                    .error(R.drawable.fui_ic_anonymous_white_24dp)
                    .build(),
                placeholder = painterResource(id = R.drawable.fui_ic_anonymous_white_24dp),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp)
                    .background(
                        Brush.sweepGradient(
                            listOf(Color.LightGray, Color.DarkGray),
                            Offset.Infinite
                        ), shape = CircleShape, 0.5F
                    )
                    .clip(CircleShape),
            )
        } else {
            Spacer(modifier = Modifier.width(30.dp))
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.Start,
        ) {

            if ((previousWasMe && !nextMessageWillMine) || (!previousWasMe && !nextMessageWillMine)) {
                Text(
                    color = GrayTitle,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    letterSpacing = 0.sp,
                    fontFamily = Roboto,
                    text = user.user_first_name,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .background(
                        GreyBottomBarBackground,
                        shape = RoundedCornerShape(30f, 30f, 30f, leftBottomBorderRadius)
                    )
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {

                if (textOverflow) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            SelectionContainer {
                                Text(
                                    operateMessageText(message = messageText),
                                    modifier = Modifier.padding(end = 7.dp),
                                    textAlign = TextAlign.Start,
                                    color = BlackTitle,
                                    fontSize = 15.sp,
                                    lineHeight = 18.sp,
                                    letterSpacing = 0.sp,
                                    fontFamily = Roboto,
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(
                                lineHeight = 14.sp,
                                fontSize = 12.sp,
                                color = GrayTitle,
                                textAlign = TextAlign.End,
                                text = messageTime,
                                modifier = Modifier
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SelectionContainer {
                                Text(
                                    operateMessageText(message = messageText),
                                    modifier = Modifier
                                        .padding(end = 0.dp)
                                        .weight(0.10f, false),
                                    textAlign = TextAlign.Start,
                                    color = BlackTitle,
                                    fontSize = 15.sp,
                                    lineHeight = 18.sp,
                                    letterSpacing = 0.sp,
                                    fontFamily = Roboto,
                                    overflow = TextOverflow.Ellipsis,
                                    onTextLayout = { textResult ->
                                        if (textResult.lineCount > 1) {
                                            textOverflow = true
                                        } else {
                                            textOverflow = false
                                        }
                                    }
                                )
                            }
                            Text(
                                lineHeight = 14.sp,
                                fontSize = 12.sp,
                                color = GrayTitle,
                                textAlign = TextAlign.End,
                                text = messageTime,
                                modifier = Modifier.requiredWidth(40.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageHasTitleWrapper(
    dataHasChanged: Long,
    modifier: Modifier,
    content: @Composable () -> Unit
) {

    val formatForTitle = SimpleDateFormat(stringResource(id = R.string.date_format_dd_MMMM_yyyy))

    val thisDayTitle = if (DateUtils.isToday(dataHasChanged)) {
        stringResource(id = R.string.message_title_today)
    } else {
        formatForTitle.format(Date(dataHasChanged))
    }

    Column {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = thisDayTitle,
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
