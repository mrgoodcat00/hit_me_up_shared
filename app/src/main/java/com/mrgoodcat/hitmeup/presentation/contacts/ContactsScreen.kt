package com.mrgoodcat.hitmeup.presentation.contacts

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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.SearchContactsScreen
import com.mrgoodcat.hitmeup.domain.model.FriendModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toChatModel
import com.mrgoodcat.hitmeup.presentation.contacts.StateParams.OpenedSearch
import com.mrgoodcat.hitmeup.presentation.contacts.StateParams.QueryString
import com.mrgoodcat.hitmeup.presentation.ui.component.BottomNavigationBar
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
fun ContactsScreen(
    navController: NavHostController,
    usersViewModel: UsersViewModel = hiltViewModel(),
    createChatWith: (ChatModel) -> Unit,
    previewProfile: (FriendModel) -> Unit
) {

    val focusManager = LocalFocusManager.current

    val screenState by usersViewModel.screenState.collectAsState()
    val contactsList = usersViewModel.users.collectAsLazyPagingItems()
    val contactsState = rememberPullToRefreshState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val filteredContactList = usersViewModel.usersFiltered.collectAsState()

    LaunchedEffect(key1 = Unit) {
        usersViewModel.updateNetworkStatus()
        usersViewModel.subscribeOnContacts()
        usersViewModel.createdNewChat.collect { data ->
            val chat = data.toChatModel()
            Timber.d("consumed: ${chat != null && chat.id.isNotEmpty()} createdNewChat $data")
            if (chat != null && chat.id.isNotEmpty()) {
                createChatWith(chat)
            }
        }
    }

    LaunchedEffect(key1 = screenState.queryString.value) {
        if (contactsList.itemCount == 0) return@LaunchedEffect

        val usersForFilter = emptyList<FriendModel>().toMutableList()

        for (item in 0..<contactsList.itemCount) {
            contactsList[item]?.let { usersForFilter.add(it) }
        }

        usersViewModel.filterContacts(screenState.queryString.value, usersForFilter)
    }

    Scaffold(
        topBar = {
            TopBarComponent(
                title = stringResource(id = R.string.contacts_screen_title),
                false,
                hasInternet = screenState.hasInternet.value
            ) {
                Row(
                    modifier = Modifier.padding(end = 0.dp)
                ) {
                    IconButton(onClick = {
                        if (!screenState.hasInternet.value) {
                            return@IconButton
                        }
                        navController.navigate(SearchContactsScreen.route)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_contact),
                            contentDescription = null,
                            modifier = Modifier.requiredSize(24.dp),
                            colorResource(id = R.color.light_blue_top_icon)
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = navBackStackEntry?.destination?.route ?: "",
                onItemClick = { navController.navigate(it.route) }
            )
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
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
                            .fillMaxWidth(if (screenState.openedSearch.value) 0.8f else 1f)
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
                                    Timber.d("ContactsScreen: ${state.hasFocus}")
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

                    Text(
                        textAlign = TextAlign.Center,
                        color = LightBlueBorder,
                        text = stringResource(id = R.string.cancel_button_title),
                        fontFamily = Roboto,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier
                            .animateContentSize()
                            .background(Color.White)
                            .requiredWidth(if (screenState.openedSearch.value) 100.dp else 0.dp)
                            .clickable {
                                usersViewModel.editScreenState(OpenedSearch(false))
                                usersViewModel.editScreenState(QueryString())
                                focusManager.clearFocus()
                            }
                    )
                }

                PullToRefreshBox(
                    contentAlignment = Alignment.Center,
                    isRefreshing = contactsList.loadState.refresh === LoadState.Loading,
                    state = contactsState,
                    onRefresh = { contactsList.refresh() }
                ) {

                    if (isNoContent(
                            filteredContactList,
                            contactsList,
                            screenState.queryString.value
                        )
                    ) {
                        EmptyDataPlaceholderComponent()
                    } else {
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .background(Color.White)
                        ) {
                            if (screenState.queryString.value.isEmpty()) {
                                items(
                                    count = contactsList.itemCount,
                                    contentType = contactsList.itemContentType(),
                                    key = contactsList.itemKey { it.useId }
                                ) { element ->
                                    val currentItem = contactsList[element] ?: return@items
                                    val isLastItem = element == contactsList.itemCount - 1
                                    ContactsListItemComponent(currentItem,
                                        isLastItem,
                                        onStartChat = {
                                            if (!screenState.hasInternet.value) {
                                                return@ContactsListItemComponent
                                            }
                                            startChatWithFriend(it, usersViewModel)
                                            focusManager.clearFocus()
                                        },
                                        onPreviewProfile = { friendModel ->
                                            previewProfile(friendModel)
                                        })
                                }
                            } else {
                                items(
                                    count = filteredContactList.value.size,
                                ) { element ->
                                    val filteredItem = filteredContactList.value[element]
                                    val isLastItem =
                                        filteredItem.useId == filteredContactList.value.last().useId
                                    ContactsListItemComponent(
                                        filteredItem,
                                        isLastItem,
                                        onStartChat = {
                                            if (!screenState.hasInternet.value) {
                                                return@ContactsListItemComponent
                                            }
                                            startChatWithFriend(it, usersViewModel)
                                            focusManager.clearFocus()
                                        },
                                        onPreviewProfile = { friendModel ->
                                            previewProfile(friendModel)
                                        })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun startChatWithFriend(it: FriendModel, usersViewModel: UsersViewModel) {
    usersViewModel.createChatWithFriend(it.useId)
}

private fun isNoContent(
    filteredContactList: State<List<FriendModel>>,
    contactsList: LazyPagingItems<FriendModel>,
    searchQueryString: String
): Boolean {
    if (searchQueryString.isNotEmpty()) {
        return filteredContactList.value.isEmpty()
    }

    return contactsList.itemCount == 0
}
