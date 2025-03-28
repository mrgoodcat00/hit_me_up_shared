package com.mrgoodcat.hitmeup.presentation.search_contacts

import android.content.Context
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.di.ApplicationScope
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.Status
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.model.FriendModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toFriendModel
import com.mrgoodcat.hitmeup.domain.usecase.CreateChatWithFriendUseCase
import com.mrgoodcat.hitmeup.domain.usecase.search_contacts.GetWholeUsersUseCase
import com.mrgoodcat.hitmeup.presentation.getStateFlow
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.HasInternet
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.OpenedSearch
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.ParamCreateChatWith
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.ParamError
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.ParamLoading
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.ParamSuccess
import com.mrgoodcat.hitmeup.presentation.search_contacts.StateParams.QueryString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val getWholeUsersUseCase: GetWholeUsersUseCase,
    private val createChatWithFriendUseCase: CreateChatWithFriendUseCase,
    private val connectivityManager: ConnectivityStateManager,
    private val savedStateHandle: SavedStateHandle,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,
) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private val _screenState: MutableStateFlow<SearchScreenState> = savedStateHandle.getStateFlow(
        applicationScope,
        SCREEN_STATE_KEY,
        SearchScreenState(),
    )

    val screenState = _screenState.asStateFlow()

    private val _searchQuery = MutableSharedFlow<String>()

    init {
        globalSearchByQuery()
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
                                Constants.ANALYTICS_EVENT_OPEN_SCREEN_SEARCH_CONTACTS
                    )
                )
        }
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

    private fun globalSearchByQuery() {
        viewModelScope.launch(exceptionHandler) {
            _searchQuery
                .buffer(BUFFERED, BufferOverflow.DROP_OLDEST)
                .onEach { delay(1000) }
                .collect { query ->
                    editScreenState(ParamLoading(false))
                    try {
                        val found = getWholeUsersUseCase.execute(query).map { it.toFriendModel() }
                        Timber.d("found $found")
                        editScreenState(ParamSuccess(found))
                    } catch (e: Exception) {
                        editScreenState(
                            ParamError(e.message ?: context.getString(R.string.load_data_error))
                        )
                    }
                }
        }
    }

    fun searchUsers(query: String = "") {
        if (!screenState.value.hasInternet.value) {
            return
        }
        editScreenState(ParamLoading(true))

        viewModelScope.launch(exceptionHandler) {
            _searchQuery.emit(query)
        }
    }

    fun createChatWithFriend(friendId: String) {
        if (!screenState.value.hasInternet.value || friendId.isEmpty()) {
            return
        }
        viewModelScope.launch(exceptionHandler) {
            editScreenState(ParamCreateChatWith(createChatWithFriendUseCase.execute(friendId)))
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

                is ParamError -> {
                    val newVal = _screenState.value.copy(error = params)
                    _screenState.emit(newVal)
                }

                is ParamLoading -> {
                    val newVal = _screenState.value.copy(loading = params)
                    _screenState.emit(newVal)
                }

                is ParamSuccess -> {
                    val newVal = _screenState.value.copy(success = params)
                    _screenState.emit(newVal)
                }

                is ParamCreateChatWith -> {
                    val newVal = _screenState.value.copy(createChatWith = params)
                    _screenState.emit(newVal)
                }
            }
        }
    }

    fun updateNetworkStatus() {
        editScreenState(HasInternet(connectivityManager.isOnline()))
    }

    companion object {
        private const val SCREEN_STATE_KEY = "screen_state_key"
    }
}

@Parcelize
data class SearchScreenState(
    val hasInternet: HasInternet = HasInternet(),
    val openedSearch: OpenedSearch = OpenedSearch(),
    val queryString: QueryString = QueryString(),
    val loading: ParamLoading = ParamLoading(),
    val success: ParamSuccess = ParamSuccess(),
    val error: ParamError = ParamError(),
    val createChatWith: ParamCreateChatWith = ParamCreateChatWith()
) : Parcelable

@Parcelize
sealed class StateParams : Parcelable {
    data class HasInternet(val value: Boolean = true) : StateParams()
    data class OpenedSearch(val value: Boolean = false) : StateParams()
    data class QueryString(val value: String = "") : StateParams()
    data class ParamLoading(val value: Boolean = false) : StateParams()
    data class ParamSuccess(val value: List<FriendModel> = emptyList()) : StateParams()
    data class ParamError(val value: String = "") : StateParams()
    data class ParamCreateChatWith(val value: ChatLocalModel? = null) : StateParams()
}