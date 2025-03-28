package com.mrgoodcat.hitmeup.presentation.chats

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.di.ApplicationScope
import com.mrgoodcat.hitmeup.di.IoDispatcher
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.Status
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toChatModel
import com.mrgoodcat.hitmeup.domain.usecase.UpdateFcmTokenUseCase
import com.mrgoodcat.hitmeup.domain.usecase.UpdateLastSeenUseCase
import com.mrgoodcat.hitmeup.domain.usecase.chats.CheckIsMyIdUseCase
import com.mrgoodcat.hitmeup.domain.usecase.chats.DeleteChatByIdUseCase
import com.mrgoodcat.hitmeup.domain.usecase.chats.GetCachedUsersUseCase
import com.mrgoodcat.hitmeup.domain.usecase.chats.GetChatListUseCase
import com.mrgoodcat.hitmeup.domain.usecase.chats.OperatePushMessageInChatsListUseCase
import com.mrgoodcat.hitmeup.domain.usecase.login.IsUserVerifiedUseCase
import com.mrgoodcat.hitmeup.domain.usecase.profile.ClearUserDataUseCase
import com.mrgoodcat.hitmeup.domain.usecase.profile.GetMyProfileUseCase
import com.mrgoodcat.hitmeup.domain.usecase.profile.LogoutUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.SendVerificationEmailUseCase
import com.mrgoodcat.hitmeup.presentation.chats.ScreenStateParams.ChatsSubscribed
import com.mrgoodcat.hitmeup.presentation.chats.ScreenStateParams.HasInternet
import com.mrgoodcat.hitmeup.presentation.chats.ScreenStateParams.UnverifiedError
import com.mrgoodcat.hitmeup.presentation.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
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
class ChatsViewModel @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val connectivityManager: ConnectivityStateManager,
    private val getChatsUseCase: GetChatListUseCase,
    private val getCachedUsersUseCase: GetCachedUsersUseCase,
    private val checkIsMyIdUseCase: CheckIsMyIdUseCase,
    private val deleteChatByIdUseCase: DeleteChatByIdUseCase,
    private val operatePushMessageInChatsListUseCase: OperatePushMessageInChatsListUseCase,
    private val sendVerificationEmailUseCase: SendVerificationEmailUseCase,
    private val isUserVerifiedUseCase: IsUserVerifiedUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val clearUserDataUseCase: ClearUserDataUseCase,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateLastSeenUseCase: UpdateLastSeenUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,

    ) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private val _chats = MutableStateFlow<PagingData<ChatModel>>(PagingData.empty())
    val chats = _chats.asStateFlow()

    private val _collocutors: MutableStateFlow<List<UserModel>> = savedStateHandle.getStateFlow(
        CoroutineScope(ioDispatcher),
        COLLOCUTORS_OBJECT_KEY,
        emptyList(),
    )
    val collocutors = _collocutors.asStateFlow()

    private val _screenState: MutableStateFlow<ChatScreenState> = savedStateHandle.getStateFlow(
        CoroutineScope(ioDispatcher),
        SCREEN_STATE_KEY,
        ChatScreenState(),
    )
    val screenState = _screenState.asStateFlow()

    init {
        Timber.d("ChatsViewModel init: ${connectivityManager.isOnline()}")
        getChatList()
        subscribeOnNetworkStatus()
        checkVerifiedState(null)
        logAnalytics()
    }

    private fun logAnalytics() {
        viewModelScope.launch(exceptionHandler) {
            hitMeUpFirebaseAnalytics
                .getAnalytics()
                .logEvent(
                    FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
                        FirebaseAnalytics.Param.SCREEN_NAME to Constants.ANALYTICS_EVENT_OPEN_SCREEN_CHAT_LIST
                    )
                )
        }
    }

    fun checkVerifiedState(state: Boolean?) {
        viewModelScope.launch(exceptionHandler) {
            isUserVerifiedUseCase.execute(state).let {
                if (!it) {
                    editScreenState(UnverifiedError(true))
                    isUserVerifiedUseCase.execute(true)
                }
            }
        }
    }

    private fun getChatList() {
        getChatsUseCase.execute()
            .distinctUntilChanged()
            .map { pagingData ->
                pagingData.map { chatLocalModel ->
                    chatLocalModel.toChatModel() ?: ChatModel()
                }
            }
            .cachedIn(viewModelScope)
            .onEach { _chats.emit(it) }
            .catch { it.printStackTrace() }
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

    suspend fun getUsersCachedInDb(userId: List<String>) {
        viewModelScope.launch(exceptionHandler) {
            val cachedUsersUseCase = getCachedUsersUseCase.execute(userId)
            _collocutors.emit(cachedUsersUseCase)
        }
    }

    fun itsMyId(userId: String): Boolean {
        return checkIsMyIdUseCase.execute(userId)
    }

    fun deleteChatById(chatId: ChatModel) {
        viewModelScope.launch(exceptionHandler) {
            deleteChatByIdUseCase.execute(chatId)
        }
    }

    fun updateNetworkStatus() {
        editScreenState(HasInternet(connectivityManager.isOnline()))
    }

    fun editScreenState(param: ScreenStateParams) {
        viewModelScope.launch(exceptionHandler) {
            when (param) {
                is ChatsSubscribed -> {
                    val newVal = _screenState.value.copy(chatsSubscribed = param)
                    _screenState.emit(newVal)
                }

                is HasInternet -> {
                    val newVal = _screenState.value.copy(hasInternet = param)
                    _screenState.emit(newVal)
                }

                is UnverifiedError -> {
                    val newVal = _screenState.value.copy(unverifiedError = param)
                    _screenState.emit(newVal)
                }
            }
        }
    }

    fun operatePushMessage(intent: Intent) {
        viewModelScope.launch(exceptionHandler) {
            operatePushMessageInChatsListUseCase.execute(intent)
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch(exceptionHandler) {
            val resultFlow = sendVerificationEmailUseCase.execute()
            resultFlow.collect { result ->
                when (result) {
                    true -> {
                        Toast.makeText(
                            context,
                            R.string.check_your_email_and_approve,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    false -> {
                        Toast.makeText(
                            context,
                            R.string.error_with_sending_email_to_approve,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch(exceptionHandler) {
            val profile = getMyProfileUseCase.execute()
            updateFcmTokenUseCase.execute(profile.user_id, true)
            clearUserDataUseCase.execute()
            logoutUseCase.execute()
        }
    }

    override fun onCleared() {
        viewModelScope.launch(exceptionHandler) {
            updateLastSeenUseCase.execute(getMyProfileUseCase.execute().user_id)
        }
        super.onCleared()
    }

    companion object {
        private const val COLLOCUTORS_OBJECT_KEY = "collocutors_object_key"
        private const val SCREEN_STATE_KEY = "screen_state_key"
    }
}

data class ChatScreenState(
    val chatsSubscribed: ChatsSubscribed = ChatsSubscribed(false),
    val hasInternet: HasInternet = HasInternet(true),
    val unverifiedError: UnverifiedError = UnverifiedError(),
) : Serializable

sealed class ScreenStateParams : Serializable {
    data class ChatsSubscribed(val value: Boolean = false) : ScreenStateParams()
    data class HasInternet(val value: Boolean = true) : ScreenStateParams()
    data class UnverifiedError(val value: Boolean = false) : ScreenStateParams()
}
