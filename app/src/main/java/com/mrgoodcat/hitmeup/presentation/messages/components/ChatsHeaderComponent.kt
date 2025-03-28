@file:OptIn(ExperimentalMaterial3Api::class)

package com.mrgoodcat.hitmeup.presentation.messages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.presentation.ui.component.ChatAvatarComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.getFormattedTimeDate
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsHeaderComponent(
    currentChat: ChatModel = ChatModel(),
    collocutors: List<UserModel> = emptyList(),
    onBackPressed: () -> Unit = {},
    onAvatarClick: (UserModel) -> Unit = {}
) {
    Surface {
        TopAppBar(
            expandedHeight = 48.dp,
            navigationIcon = {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .width(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_top_back_navigation),
                        contentDescription = "",
                        modifier = Modifier.size(24.dp, 24.dp)
                    )
                }
            },
            title = {
                Row(
                    modifier = Modifier
                        .clickable {
                            if (collocutors.size > 1) {
                                collocutors.firstOrNull()?.let {
                                    onAvatarClick(it)
                                }
                            } else {
                                collocutors.firstOrNull()?.let {
                                    onAvatarClick(it)
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .width(36.dp)
                    ) {
                        ChatAvatarComponent(collocutors, currentChat, Size(36F, 36F))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp)
                    ) {
                        Row {
                            val builder = StringBuilder()

                            if (collocutors.size > 1) {
                                collocutors.forEachIndexed { index, userModel ->
                                    builder.append("${userModel.user_first_name} ${userModel.user_last_name}")
                                    if (index != collocutors.size - 1) builder.append(", ")
                                }
                            } else if (collocutors.isNotEmpty()) {
                                builder.append("${collocutors.first().user_first_name} ${collocutors.first().user_last_name}")
                            }

                            Text(
                                text = builder.toString(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = Roboto,
                                fontSize = 18.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.W500,
                                color = BlackTitle,
                                modifier = Modifier,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Row {
                            val participantsTitle = if (collocutors.size > 1) {
                                stringResource(
                                    id = R.string.collocutors_in_chat_format,
                                    collocutors.size
                                )
                            } else {
                                getFormattedTimeDate(
                                    timestamp =
                                    if (collocutors.isEmpty()) 0L
                                    else collocutors.first().user_last_seen
                                )
                            }

                            Text(
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                text = participantsTitle,
                                fontFamily = Roboto,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 14.sp,
                                color = GrayTitle,
                                modifier = Modifier
                            )
                        }
                    }
                }
            },
            colors = TopAppBarColors(
                containerColor = Color.White,
                scrolledContainerColor = Color.Green,
                navigationIconContentColor = LightBlueBorder,
                titleContentColor = BlackTitle,
                actionIconContentColor = Color.Blue
            )
        )
    }

}