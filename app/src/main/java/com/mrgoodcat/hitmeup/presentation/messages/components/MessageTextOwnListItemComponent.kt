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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.presentation.messages.operateMessageText
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto
import com.mrgoodcat.hitmeup.presentation.ui.theme.WhiteTransparentTitle
import java.text.SimpleDateFormat
import java.util.Date

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MessageTextOwnComponent(
    message: MessageModel = MessageModel(),
    messageText: String = "",
    previousWasMe: Boolean = false,
    nextMessageWillMine: Boolean = false,
    dataHasChanged: Long = 0L
) {
    val format = SimpleDateFormat(stringResource(id = R.string.time_format_HH_mm))
    val messageTime = format.format(Date(message.timestamp))

    val messageTopPadding: Dp = if (nextMessageWillMine) {
        3.dp
    } else {
        15.dp
    }

    val rightBottomBorderRadius: Float =
        if ((!previousWasMe && nextMessageWillMine) || (!previousWasMe && !nextMessageWillMine)) {
            5F
        } else {
            30f
        }

    if (dataHasChanged != 0L) {
        MessageOwnHasTitleWrapper(
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
                Content(rightBottomBorderRadius, messageText, messageTime)
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
            Content(
                rightBottomBorderRadius = rightBottomBorderRadius,
                messageText = messageText,
                messageTime = messageTime
            )
        }
    }
}

@Composable
fun Content(rightBottomBorderRadius: Float, messageText: String, messageTime: String) {
    Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalAlignment = Alignment.End,
    ) {
        var textOverflow by remember {
            mutableStateOf(false)
        }

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .background(
                    LightBlueBorder,
                    shape = RoundedCornerShape(30f, 30f, rightBottomBorderRadius, 30f)
                )
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .align(Alignment.End)
        ) {
            if (textOverflow) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 3.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                operateMessageText(message = messageText),
                                modifier = Modifier.padding(start = 0.dp),
                                textAlign = TextAlign.Start,
                                color = Color.White,
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
                            textAlign = TextAlign.End,
                            text = messageTime,
                            color = WhiteTransparentTitle,
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        SelectionContainer {
                            Text(
                                operateMessageText(message = messageText),
                                modifier = Modifier
                                    .padding(start = 0.dp, end = 10.dp)
                                    .weight(0.10f, false),
                                textAlign = TextAlign.Start,
                                color = Color.White,
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
                            textAlign = TextAlign.End,
                            text = messageTime,
                            color = WhiteTransparentTitle,
                            modifier = Modifier.requiredWidth(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageOwnHasTitleWrapper(
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