package com.mrgoodcat.hitmeup.presentation.contacts

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.di.ApplicationScope
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.Status
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.model.FriendModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toFriendModel
import com.mrgoodcat.hitmeup.domain.usecase.CreateChatWithFriendUseCase
import com.mrgoodcat.hitmeup.domain.usecase.UpdateLastSeenUseCase
import com.mrgoodcat.hitmeup.domain.usecase.contacts.GetAllUsersUseCase
import com.mrgoodcat.hitmeup.domain.usecase.contacts.SubscribeOnContactsListChangesUseCase
import com.mrgoodcat.hitmeup.domain.usecase.profile.GetMyProfileUseCase
import com.mrgoodcat.hitmeup.presentation.contacts.StateParams.HasInternet
import com.mrgoodcat.hitmeup.presentation.contacts.StateParams.OpenedSearch
import com.mrgoodcat.hitmeup.presentation.contacts.StateParams.QueryString
import com.mrgoodcat.hitmeup.presentation.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val createChatWithFriendUseCase: CreateChatWithFriendUseCase,
    private val subscribeOnContactsListChangesUseCase: SubscribeOnContactsListChangesUseCase,
    private val connectivityManager: ConnectivityStateManager,
    private val updateLastSeenUseCase: UpdateLastSeenUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private val _users = MutableStateFlow(PagingData.empty<FriendModel>())
    val users = _users.asStateFlow()

    private val _screenState: MutableStateFlow<ContactsScreenState> = savedStateHandle.getStateFlow(
        applicationScope, SCREEN_STATE_KEY, ContactsScreenState()
    )
    val screenState = _screenState.asStateFlow()

    private val _usersFiltered: MutableStateFlow<List<FriendModel>> =
        savedStateHandle.getStateFlow(applicationScope, USERS_FILTERED_KEY, emptyList())

    val usersFiltered = _usersFiltered

    private val _createdNewChat = MutableSharedFlow<ChatLocalModel?>()
    val createdNewChat = _createdNewChat.asSharedFlow()

    init {
        getFriends()
        subscribeOnNetworkStatus()
        logAnalytics()
    }

    private fun logAnalytics() {
        viewModelScope.launch(exceptionHandler) {
            hitMeUpFirebaseAnalytics
                .getAnalytics()
                .logEvent(
                    FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
                        FirebaseAnalytics.Param.SCREEN_NAME to
                                Constants.ANALYTICS_EVENT_OPEN_SCREEN_CONTACTS
                    )
                )
        }
    }

    private fun getFriends() {
        getAllUsersUseCase.execute()
            .distinctUntilChanged()
            .map { pagingData -> pagingData.map { it.toFriendModel() } }
            .cachedIn(viewModelScope)
            .catch { it.printStackTrace() }
            .onEach { _users.emit(it) }
            .launchIn(viewModelScope)
    }

    private fun subscribeOnNetworkStatus() {
        viewModelScope.launch(exceptionHandler) {
            connectivityManager
                .observeNetworkStatus()
                .collect {
                    when (it) {
                        Status.Available -> {
                            editScreenState(HasInternet(true))
                        }

                        else -> {
                            editScreenState(HasInternet(false))
                        }
                    }
                }
        }
    }

    fun filterContacts(query: String, contactsList: MutableList<FriendModel>) {
        viewModelScope.launch(exceptionHandler) {
            val find = contactsList.filter {
                (it.userFirstName.contains(query, true)
                        || it.userLastName.contains(query, true)
                        || it.userEmail.contains(query, true))
            }
            _usersFiltered.emit(find)
        }
    }

    fun createChatWithFriend(friendId: String) {
        viewModelScope.launch(exceptionHandler) {
            val createdChat = createChatWithFriendUseCase.execute(friendId)
            Timber.d("emited: $createdChat")
            _createdNewChat.emit(createdChat)
        }
    }

    fun subscribeOnContacts() {
        viewModelScope.launch(exceptionHandler) {
            subscribeOnContactsListChangesUseCase.execute()
        }
    }

    fun editScreenState(params: StateParams) {
        viewModelScope.launch(exceptionHandler) {
            when (params) {
                is HasInternet -> {
                    val newVal = _screenState.value.copy(hasInternet = params)
                    _screenState.emit(newVal)
                }

                is OpenedSearch -> {
                    val newVal = _screenState.value.copy(openedSearch = params)
                    _screenState.emit(newVal)
                }

                is QueryString -> {
                    val newVal = _screenState.value.copy(queryString = params)
                    _screenState.emit(newVal)
                }
            }
        }
    }

    fun updateNetworkStatus() {
        editScreenState(HasInternet(connectivityManager.isOnline()))
    }

    override fun onCleared() {
        viewModelScope.launch {
            updateLastSeenUseCase.execute(getMyProfileUseCase.execute().user_id)
        }
        super.onCleared()
    }

    companion object {
        private const val USERS_FILTERED_KEY = "users_filtered_key"
        private const val SCREEN_STATE_KEY = "screen_state_key"
    }
}

data class ContactsScreenState(
    val hasInternet: HasInternet = HasInternet(),
    val openedSearch: OpenedSearch = OpenedSearch(),
    val queryString: QueryString = QueryString(),
) : Serializable

sealed class StateParams : Serializable {
    data class HasInternet(val value: Boolean = true) : StateParams()
    data class OpenedSearch(val value: Boolean = false) : StateParams()
    data class QueryString(val value: String = "") : StateParams()
}