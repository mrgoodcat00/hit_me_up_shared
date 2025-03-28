package com.mrgoodcat.hitmeup.presentation.chats

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.BROADCAST_CHAT_ID_EXTRA_KEY
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.presentation.chats.components.ChatListItemComponent
import com.mrgoodcat.hitmeup.presentation.chats.components.ConfirmationDialog
import com.mrgoodcat.hitmeup.presentation.chats.components.ConfirmationDialogParams
import com.mrgoodcat.hitmeup.presentation.findActivity
import com.mrgoodcat.hitmeup.presentation.ui.component.BottomNavigationBar
import com.mrgoodcat.hitmeup.presentation.ui.component.EmptyDataPlaceholderComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.TopBarComponent

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    navController: NavHostController,
    chatsViewModel: ChatsViewModel = hiltViewModel<ChatsViewModel>(),
    chatClick: (ChatModel) -> Unit,
    previewProfile: (UserModel) -> Unit,
) {
    val screenState by chatsViewModel.screenState.collectAsState()
    val chatsList = chatsViewModel.chats.collectAsLazyPagingItems()
    val chatsState = rememberPullToRefreshState()
    val localContext = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var notificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                localContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED
            )
        } else {
            mutableStateOf(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { result ->
        notificationPermission = result
        if (!notificationPermission && localContext.findActivity()
                .shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            Toast.makeText(
                localContext,
                R.string.notification_permissions_permanently_denied,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (!notificationPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DisposableEffect(key1 = Unit) {
        val intent = localContext.findActivity().intent
        if (intent != null && intent.hasExtra(BROADCAST_CHAT_ID_EXTRA_KEY)) {
            val chatId = intent.extras?.getString(BROADCAST_CHAT_ID_EXTRA_KEY, "") ?: ""
            if (chatId.isNotEmpty()) {
                chatsViewModel.operatePushMessage(intent)
                val chat = ChatModel(id = chatId)
                chatClick(chat)
            }
        }

        onDispose {
            if (intent != null && intent.hasExtra(BROADCAST_CHAT_ID_EXTRA_KEY)) {
                intent.replaceExtras(Bundle())
            }
        }
    }

    LaunchedEffect(key1 = chatsList.itemCount) {
        val userIds = HashSet<String>()
        for (i in 0..<chatsList.itemCount) {
            chatsList[i]?.participantIds?.keys?.map { participantId ->
                userIds.add(participantId)
            }
        }
        chatsViewModel.getUsersCachedInDb(userIds.toList())
    }

    LaunchedEffect(key1 = Unit) {
        chatsViewModel.updateNetworkStatus()
//        if (!screenState.chatsSubscribed.value) {
//            chatsViewModel.editScreenState(ScreenStateParams.ChatsSubscribed(chatsViewModel.subscribeMessages()))
//        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        topBar = {
            TopBarComponent(
                stringResource(id = R.string.chat_screen_title),
                hasInternet = screenState.hasInternet.value
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = navBackStackEntry?.destination?.route ?: "",
                onItemClick = { navController.navigate(it.route) }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(paddingValues)
                .background(Color.White)
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
            isRefreshing = chatsList.loadState.refresh == LoadState.Loading,
            state = chatsState,
            onRefresh = { chatsList.refresh() }
        ) {
            if (chatsList.itemCount == 0) {
                EmptyDataPlaceholderComponent()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    items(
                        count = chatsList.itemCount,
                        contentType = chatsList.itemContentType(),
                        key = chatsList.itemKey { item ->
                            item.id
                        }
                    ) { element ->
                        val chat = chatsList[element]
                        if (chat != null) {
                            ChatListItemComponent(chat, chatsViewModel,
                                deleteChat = { chatToDelete ->
                                    chatsViewModel.deleteChatById(chatToDelete)
                                }, openChat = { clickedChat ->
                                    chatClick(clickedChat)
                                },
                                previewProfile = previewProfile
                            )
                        }
                    }
                }
            }
        }

        if (screenState.unverifiedError.value) {
            ConfirmationDialog(
                ConfirmationDialogParams(
                    title = stringResource(id = R.string.verifi_email_dialog_title),
                    text = stringResource(id = R.string.email_not_verified_error),
                    cancelButtonText = stringResource(id = R.string.cancel_button_title),
                    confirmButtonText = stringResource(id = R.string.send_one_more_verification_button)
                ),
                dismiss = null,
                cancel = {
                    chatsViewModel.editScreenState(ScreenStateParams.UnverifiedError(false))
                    chatsViewModel.logoutUser()
                }, confirm = {
                    chatsViewModel.sendVerificationEmail()
                    chatsViewModel.editScreenState(ScreenStateParams.UnverifiedError(false))
                    chatsViewModel.logoutUser()
                })
        }

    }
}