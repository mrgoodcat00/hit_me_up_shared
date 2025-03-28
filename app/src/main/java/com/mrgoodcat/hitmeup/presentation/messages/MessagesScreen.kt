package com.mrgoodcat.hitmeup.presentation.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.MessageContentType
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.domain.model.extensions.getContentToObject
import com.mrgoodcat.hitmeup.presentation.messages.ScreenStateParams.ImagePreview
import com.mrgoodcat.hitmeup.presentation.messages.components.BottomSendMessageComponent
import com.mrgoodcat.hitmeup.presentation.messages.components.ChatsHeaderComponent
import com.mrgoodcat.hitmeup.presentation.messages.components.MessageImageComponent
import com.mrgoodcat.hitmeup.presentation.messages.components.MessageImageOwnComponent
import com.mrgoodcat.hitmeup.presentation.messages.components.MessageTextComponent
import com.mrgoodcat.hitmeup.presentation.messages.components.MessageTextOwnComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.EmptyDataPlaceholderComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.NoInternetMessageComponent
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightGreyBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavHostController,
    messagesViewModel: MessagesViewModel = hiltViewModel<MessagesViewModel>(),
    chatId: String,
    onPreviewClicked: (UserModel) -> Unit
) {

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    val screenState by messagesViewModel.screenState.collectAsState()
    val messagesList = messagesViewModel.messages.collectAsLazyPagingItems()
    val pullToRefreshState = rememberPullToRefreshState()

    var latch by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        chatId.let {
            messagesViewModel.getMessages(chatId)
            messagesViewModel.subscribeMessages(chatId)
            messagesViewModel.getChatDataCachedInDb(chatId)
        }
    }

    LaunchedEffect(key1 = screenState.currentChat) {
        if (screenState.currentChat.value.id.isNotEmpty()) {
            messagesViewModel.dropUnReadChatCounter(screenState.currentChat.value.id)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .systemBarsPadding()
            .background(Color.White),
        topBar = {
            Column {
                ChatsHeaderComponent(
                    currentChat = screenState.currentChat.value,
                    collocutors = screenState.collocutors.value.filter {
                        messagesViewModel.getMyProfile().userId != it.user_id
                    },
                    onBackPressed = {
                        if (latch == 0) return@ChatsHeaderComponent
                        navController.popBackStack()
                        latch--
                    },
                    onAvatarClick = {
                        onPreviewClicked(it)
                    })
                HorizontalDivider(color = LightGreyBorder, thickness = 0.5.dp)

                NoInternetMessageComponent(hasInternet = screenState.hasInternet.value)
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .imePadding()
            ) {
                HorizontalDivider(color = LightGreyBorder, thickness = 0.5.dp)
                BottomSendMessageComponent(
                    messageViewModel = messagesViewModel,
                    chatId = chatId,
                    hasInternet = screenState.hasInternet.value
                )
            }
        }
    ) { paddingValues ->
        Surface(
            Modifier
                .padding(paddingValues)
                .background(Color.White)
                .fillMaxWidth()
                .fillMaxHeight()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            PullToRefreshBox(
                contentAlignment = Alignment.Center,
                isRefreshing = messagesList.loadState.refresh === LoadState.Loading,
                state = pullToRefreshState,
                onRefresh = {
                    messagesList.refresh()
                    messagesViewModel.scrollToEnd()
                }
            ) {

                if (messagesList.itemCount == 0) {
                    EmptyDataPlaceholderComponent(stringResource(id = R.string.empty_message_list_text))
                } else {
                    LazyColumn(
                        state = messagesViewModel.scrollState,
                        reverseLayout = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(Color.White)
                            .padding(start = 15.dp, end = 15.dp, bottom = 0.dp),
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }

                        items(
                            count = messagesList.itemCount,
                            contentType = messagesList.itemContentType(),
                            key = messagesList.itemKey { it.id }
                        ) { element ->
                            val message = messagesList[element] ?: return@items
                            val prevMessageWasMine =
                                messagesViewModel.isPrevMessWasMine(messagesList, element, message)
                            val nextMessageWillMine =
                                messagesViewModel.isNextMessWillMine(messagesList, element, message)
                            val dateHasChanged =
                                messagesViewModel.isDataHasChanged(messagesList, element, message)

                            val type = message.getContentToObject()

                            if (messagesViewModel.getMyProfile().userId == message.sender) {
                                when (type) {
                                    is MessageContentType.SimpleText -> {
                                        MessageTextOwnComponent(
                                            message = message,
                                            messageText = type.text,
                                            previousWasMe = prevMessageWasMine,
                                            nextMessageWillMine = nextMessageWillMine,
                                            dataHasChanged = dateHasChanged,
                                        )
                                    }

                                    is MessageContentType.TextWithImage -> {

                                    }

                                    is MessageContentType.Image -> {
                                        MessageImageOwnComponent(
                                            message = message,
                                            previousWasMe = prevMessageWasMine,
                                            nextMessageWillMine = nextMessageWillMine,
                                            dataHasChanged = dateHasChanged,
                                            onImageClick = { uri ->
                                                messagesViewModel.editScreenState(ImagePreview(uri))
                                            }
                                        )
                                    }
                                }
                            } else {
                                when (type) {
                                    is MessageContentType.SimpleText -> {
                                        val user = screenState.collocutors.value.find {
                                            it.user_id == message.sender
                                        } ?: UserModel()
                                        MessageTextComponent(
                                            messageModel = message,
                                            messageText = type.text,
                                            user = user,
                                            previousWasMe = prevMessageWasMine,
                                            nextMessageWillMine = nextMessageWillMine,
                                            dataHasChanged = dateHasChanged
                                        )
                                    }

                                    is MessageContentType.TextWithImage -> {

                                    }

                                    is MessageContentType.Image -> {
                                        val user = screenState.collocutors.value.find {
                                            it.user_id == message.sender
                                        } ?: UserModel()
                                        MessageImageComponent(
                                            messageModel = message,
                                            user = user,
                                            previousWasMe = prevMessageWasMine,
                                            nextMessageWillMine = nextMessageWillMine,
                                            dataHasChanged = dateHasChanged,
                                            onImageClick = { uri ->
                                                messagesViewModel.editScreenState(ImagePreview(uri))
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }

                if (screenState.imagePreview.value.isNotEmpty()) {
                    Dialog(
                        properties = DialogProperties(
                            usePlatformDefaultWidth = false,
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true,
                            decorFitsSystemWindows = true
                        ),
                        onDismissRequest = {
                            messagesViewModel.editScreenState(ImagePreview())
                        },
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clickable {
                                    messagesViewModel.editScreenState(ImagePreview())
                                },
                            color = Color(
                                0F, 0F, 0F, 0.18F
                            )
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(screenState.imagePreview.value)
                                    .crossfade(true)
                                    .error(R.drawable.ic_add_image)
                                    .build(),
                                contentDescription = "",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun operateMessageText(message: String): AnnotatedString {
    val textArray = message.split(" ")
    return buildAnnotatedString {
        SelectionContainer {
            for (item in textArray) {
                if (!isContainsUrl(item)) {
                    SelectionContainer {
                        append("$item ")
                    }
                } else {
                    withLink(
                        LinkAnnotation.Url(
                            item,
                            TextLinkStyles(
                                style = SpanStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline,
                                    letterSpacing = 0.sp,
                                )
                            )
                        )
                    ) {
                        SelectionContainer {
                            append("$item ")
                        }
                    }
                }
            }
        }
    }
}

fun isContainsUrl(string: String): Boolean {
    val regexp =
        """((http|ftp|https)://([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&:/~+#-]*[\w@?^=%&/~+#-])?)"""
    return string.contains(regex = Regex(regexp))
}