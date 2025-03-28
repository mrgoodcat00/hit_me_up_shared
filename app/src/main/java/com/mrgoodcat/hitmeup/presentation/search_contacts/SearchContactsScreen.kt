package com.mrgoodcat.hitmeup.presentation.search_contacts

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.FriendModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toChatModel
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.OpenedSearch
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.ParamCreateChatWith
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.ParamError
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.ParamLoading
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.QueryString
import com.mrgoodcat.hitmeup.presentation.ui.component.ContactsListItemComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.EmptyDataPlaceholderComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.ResizableInputComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.TopBarComponent
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyBottomBarBackground
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyMenuText
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContactsScreen(
    navController: NavHostController,
    usersViewModel: SearchUsersViewModel = hiltViewModel(),
    createChatWith: (ChatModel) -> Unit,
    previewProfile: (FriendModel) -> Unit
) {
    val screenState by usersViewModel.screenState.collectAsState()
    val usersListState = rememberPullToRefreshState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var latch by remember { mutableIntStateOf(1) }

    LaunchedEffect(key1 = screenState.error) {
        Timber.d("ContactsScreen: searchResult ${screenState.loading}")

        if (screenState.error.value.isNotEmpty()) {
            context.let {
                Toast.makeText(it, screenState.error.value, Toast.LENGTH_SHORT).show()
            }
            usersViewModel.editScreenState(ParamError())
            usersViewModel.editScreenState(ParamLoading(false))
            return@LaunchedEffect
        }
    }

    LaunchedEffect(key1 = screenState.createChatWith.value) {
        val chat = screenState.createChatWith.value.toChatModel() ?: return@LaunchedEffect
        Timber.d("SearchContactsScreen: createdNewChat $chat")
        if (chat.id.isNotEmpty()) {
            createChatWith(chat)
            usersViewModel.editScreenState(ParamCreateChatWith())
        }
    }

    LaunchedEffect(key1 = screenState.queryString.value) {
        Timber.d("ContactsScreen query:${screenState.queryString.value}")
        usersViewModel.searchUsers(screenState.queryString.value)
    }

    LaunchedEffect(key1 = Unit) {
        if (screenState.queryString.value.isNotEmpty()) {
            usersViewModel.editScreenState(OpenedSearch(true))
        }
    }

    Scaffold(
        topBar = {
            TopBarComponent(
                title = stringResource(id = R.string.global_search_screen_title),
                false,
                screenState.hasInternet.value
            ) {
                Row(
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (latch == 0) return@IconButton
                            navController.popBackStack()
                            latch--
                        },
                        modifier = Modifier.width(60.dp)
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.cancel_button_title),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontFamily = Roboto,
                            color = LightBlueBorder
                        )
                    }
                }
            }
        },
        bottomBar = {}
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .background(Color.White)
                .fillMaxHeight()
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (screenState.openedSearch.value) Arrangement.SpaceBetween else Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(Color.White)
                        .padding(horizontal = 15.dp, vertical = 7.dp)
                ) {

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(GreyBottomBarBackground, RoundedCornerShape(percent = 50))
                            .padding(vertical = 5.dp)
                            .height(40.dp)
                            .fillMaxWidth()
                    ) {
                        ResizableInputComponent(
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_search),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .requiredWidth(24.dp)
                                        .clickable { },
                                    colorResource(id = R.color.gray_menu_text)
                                )
                            },
                            value = screenState.queryString.value,
                            onValueChange = {
                                usersViewModel.editScreenState(QueryString(it))
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontFamily = Roboto,
                                textAlign = if (screenState.openedSearch.value) TextAlign.Start else TextAlign.Center,
                                color = BlackTitle,
                            ),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = BlackTitle,
                                cursorColor = LightBlueBorder,
                            ),
                            maxLines = 1,
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.contact_search_field),
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    fontFamily = Roboto,
                                    color = GreyMenuText,
                                    textAlign = if (screenState.openedSearch.value) TextAlign.Start else TextAlign.Center
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .onFocusChanged { state ->
                                    usersViewModel.editScreenState(OpenedSearch(state.hasFocus))
                                }
                                .fillMaxWidth(0.86f)
                                .height(26.dp),
                        )

                        IconButton(
                            modifier = Modifier
                                .animateContentSize()
                                .requiredHeight(if (screenState.queryString.value.isNotEmpty()) 24.dp else 0.dp),
                            onClick = {
                                usersViewModel.editScreenState(QueryString())
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clear_search),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(GrayTitle, RoundedCornerShape(16.dp)),
                                colorResource(id = R.color.white)
                            )
                        }
                    }
                }

                PullToRefreshBox(
                    contentAlignment = Alignment.Center,
                    isRefreshing = screenState.loading.value,
                    state = usersListState,
                    onRefresh = { usersViewModel.searchUsers(screenState.queryString.value) }
                ) {

                    if (screenState.success.value.isEmpty()) {
                        EmptyDataPlaceholderComponent()
                    } else {
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .background(Color.White)
                        ) {
                            items(
                                count = screenState.success.value.size,
                                key = { int ->
                                    screenState.success.value[int].useId
                                }
                            ) { element ->
                                val currentItem = screenState.success.value[element]
                                val isLastItem = element == screenState.success.value.size - 1
                                ContactsListItemComponent(currentItem, isLastItem,
                                    onStartChat = {
                                        usersViewModel.createChatWithFriend(it.useId)
                                        focusManager.clearFocus()
                                    }, onPreviewProfile = {
                                        previewProfile(it)
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}