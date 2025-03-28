package com.mrgoodcat.hitmeup.presentation.chats.components

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.presentation.chats.ChatsViewModel
import com.mrgoodcat.hitmeup.presentation.ui.component.ChatAvatarComponent
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyBottomBarBackground
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto
import com.mrgoodcat.hitmeup.presentation.ui.theme.UnreadedMessagesColor
import java.text.SimpleDateFormat
import java.util.Date


@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun ChatListItemComponent(
    element: ChatModel = ChatModel(),
    chatsViewModel: ChatsViewModel = hiltViewModel(),
    deleteChat: (ChatModel) -> Unit = {},
    openChat: (ChatModel) -> Unit = {},
    previewProfile: (UserModel) -> Unit = {},
) {
    val state = chatsViewModel.collocutors.collectAsState().value
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    val showAlertDialog = remember { mutableStateOf(false) }
    val alertDialogParams = remember { mutableStateOf(ConfirmationDialogParams()) }

    val lastMessageAuthor = chatsViewModel.itsMyId(element.lastMessageSender)

    val collocutors = state.filter {
        element.participantIds.containsKey(it.user_id) && !chatsViewModel.itsMyId(it.user_id)
    }

    val format =
        SimpleDateFormat(
            if (DateUtils.isToday(element.lastMessageTimestamp))
                stringResource(id = R.string.time_format_HH_mm) else stringResource(
                id = R.string.date_format_with_dots_dd_MM_yyyy
            )
        )
    val lastMessageDate = format.format(Date(element.lastMessageTimestamp))

    if (showAlertDialog.value) {
        ConfirmationDialog(alertDialogParams.value, dismiss = {
            showAlertDialog.value = false
        }, cancel = {
            showAlertDialog.value = false
        }, confirm = {
            deleteChat(element)
            showAlertDialog.value = false
        })
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(70.dp)
            .background(
                if (element.unreadedCounter != 0) GreyBottomBarBackground else Color.White
            )
            .padding(start = 15.dp, top = 8.dp, bottom = 8.dp),

        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .requiredWidth(67.dp)
                    .padding(end = 13.dp)
                    .fillMaxHeight()
                    .clickable {
                        if (collocutors.size > 1) {

                        } else {
                            previewProfile(collocutors.first())
                        }
                    },
            ) {
                ChatAvatarComponent(
                    collocutor = collocutors,
                    currentChat = element,
                    iconSize = Size(54f, 54f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(if (DateUtils.isToday(element.lastMessageTimestamp)) 0.83f else 0.7f)
                    .combinedClickable(
                        onLongClick = {
                            expanded.value = true
                        }, onClick = {
                            openChat(element)
                        }),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                ChatTitleComponent(collocutors, Modifier.fillMaxWidth())

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text =
                    if (lastMessageAuthor)
                        stringResource(
                            id = R.string.last_message_header_format,
                            element.lastMessageText
                        )
                    else element.lastMessageText,
                    lineHeight = 24.sp,
                    fontFamily = Roboto,
                    fontSize = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    color = GrayTitle,
                    maxLines = 1,
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(end = 15.dp)
                .defaultMinSize(if (DateUtils.isToday(element.lastMessageTimestamp)) 160.dp else 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                text = lastMessageDate,
                color = GrayTitle,
                fontFamily = Roboto,
                lineHeight = 24.sp,
                fontSize = 12.sp,
            )
            if (element.unreadedCounter != 0) {
                Text(
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .drawBehind {
                            drawCircle(UnreadedMessagesColor, radius = 30F)
                        },
                    text = element.unreadedCounter.toString(),
                    color = Color.White,
                    fontFamily = Roboto,
                    lineHeight = 24.sp,
                    fontSize = 12.sp,
                )
            }
        }

        DropdownMenuComponent(
            expanded = expanded.value,
            menuItems = arrayListOf(MenuParams(stringResource(id = R.string.dropdown_menu_item_delete))),
            dismissRequest = { expanded.value = false }
        ) {
            expanded.value = false
            alertDialogParams.value.title = context.getString(R.string.dropdown_menu_item_delete)
            alertDialogParams.value.text = context.getString(R.string.delete_chat_dialog_text)
            showAlertDialog.value = true
        }
    }
}
