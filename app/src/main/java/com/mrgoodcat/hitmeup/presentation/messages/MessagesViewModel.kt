package com.mrgoodcat.hitmeup.presentation.messages

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.map
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel.Companion.createMessageContent
import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel
import com.mrgoodcat.hitmeup.data.repostory.UploadMediaResult
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.Status
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.model.ChatModel
import com.mrgoodcat.hitmeup.domain.model.MessageContentType
import com.mrgoodcat.hitmeup.domain.model.MessageModel
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.domain.model.UserProfileModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toMessagesModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserProfileModel
import com.mrgoodcat.hitmeup.domain.usecase.GetUserByIdFromApiUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.DropUnReadMessagesCounterUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.GetChatUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.GetMessagesByChatIdUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.GetMyProfileUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.PrependNewMessageInChatUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.SendMessageUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.SubscribeOnChatMessagesUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.UploadImageUseCase
import com.mrgoodcat.hitmeup.presentation.messages.ScreenStateParams.Collocutors
import com.mrgoodcat.hitmeup.presentation.messages.ScreenStateParams.CurrentChat
import com.mrgoodcat.hitmeup.presentation.messages.ScreenStateParams.HasInternet
import com.mrgoodcat.hitmeup.presentation.messages.ScreenStateParams.ImagePreview
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getMessagesByChatIdUseCase: GetMessagesByChatIdUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getChatUseCase: GetChatUseCase,
    private val getUserFromApiById: GetUserByIdFromApiUseCase,
    private val prependNewMessage: PrependNewMessageInChatUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val subscribeOnChatMessagesUseCase: SubscribeOnChatMessagesUseCase,
    private val uploadImage: UploadImageUseCase,
    private val connectivityManager: ConnectivityStateManager,
    private val dropUnReadMessagesCounterUseCase: DropUnReadMessagesCounterUseCase,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,
) : ViewModel() {
    private lateinit var userProfile: UserProfileLocalModel

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private val _screenState = MutableStateFlow(MessageScreenState())
    val screenState = _screenState.asStateFlow()

    private val _messages = MutableStateFlow(PagingData.empty<MessageModel>())
    val messages = _messages.asStateFlow()

    private val _uploadResult = MutableSharedFlow<UploadResult>()
    val uploadResult = _uploadResult.asSharedFlow()

    val scrollState = LazyListState()

    init {
        viewModelScope.launch(exceptionHandler) {
            viewModelScope.launch {
                userProfile = getMyProfileUseCase.execute()
            }.join()
            subscribeOnNetworkStatus()
            updateNetworkStatus()
        }
        logAnalytics()
    }

    private fun logAnalytics() {
        viewModelScope.launch(exceptionHandler) {

            hitMeUpFirebaseAnalytics
                .getAnalytics()
                .logEvent(
                    FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
                        FirebaseAnalytics.Param.SCREEN_NAME to
                                Constants.ANALYTICS_EVENT_OPEN_SCREEN_CHAT_MESSAGES
                    )
                )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("Message view model onCleared")
    }

    private fun updateNetworkStatus() {
        editScreenState(HasInternet(connectivityManager.isOnline()))
    }

    private fun compareIfDateHasChanged(l1: List<String>, l2: List<String>): Boolean {
        if (l1.size != l2.size) {
            return false
        } else if (l1[0] != (l2[0]) || l1[1] != l2[1] || l1[2] != l2[2]) {
            return true
        } else {
            return false
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

    private suspend fun getUsersCachedInDb(userId: List<String>) {
        viewModelScope.launch(exceptionHandler) {
            val resultArray: MutableList<UserModel> = ArrayList()
            userId.map { id ->
                if (userProfile.user_id != id) {
                    val userFromApiById = getUserFromApiById.execute(id, withUpdate = true)
                    resultArray.add(userFromApiById.toUserModel())
                }
            }
            editScreenState(Collocutors(resultArray))
        }
    }

    fun editScreenState(param: ScreenStateParams) {
        viewModelScope.launch(exceptionHandler) {
            when (param) {
                is Collocutors -> {
                    val newVal = _screenState.value.copy(collocutors = param)
                    _screenState.emit(newVal)
                }

                is CurrentChat -> {
                    val newVal = _screenState.value.copy(currentChat = param)
                    _screenState.emit(newVal)
                }

                is HasInternet -> {
                    val newVal = _screenState.value.copy(hasInternet = param)
                    _screenState.emit(newVal)
                }

                is ImagePreview -> {
                    val newVal = _screenState.value.copy(imagePreview = param)
                    _screenState.emit(newVal)
                }
            }
        }
    }

    fun sendTextMessage(textMessage: String, chatId: String) {
        viewModelScope.launch(exceptionHandler) {
            val message = MessageModel(
                id = UUID.randomUUID().toString(),
                sender = getMyProfile().userId,
                timestamp = System.currentTimeMillis(),
                chat_id = chatId,
                content = createMessageContent(MessageContentType.SimpleText(text = textMessage))
            )
            sendMessageUseCase.execute(message, chatId)
        }
    }

    fun sendImageMessage(uri: Uri, chatId: String) {
        viewModelScope.launch(exceptionHandler) {
            uploadImage.execute(uri, chatId).collect { uploadResult ->
                when (uploadResult) {
                    is UploadMediaResult.Error -> {
                        Timber.d("Error: $uploadResult")
                        _uploadResult.emit(UploadResult.Error(uploadResult.message))
                    }

                    is UploadMediaResult.Loading -> {
                        Timber.d("Loading: $uploadResult")
                        val percent = ((uploadResult.transferred * 100) / uploadResult.total)
                        _uploadResult.emit(UploadResult.Loading(percent.toInt()))
                    }

                    is UploadMediaResult.Success -> {
                        Timber.d("Success: $uploadResult")

                        val message = MessageModel(
                            id = UUID.randomUUID().toString(),
                            sender = getMyProfile().userId,
                            timestamp = System.currentTimeMillis(),
                            chat_id = chatId,
                            content = createMessageContent(MessageContentType.Image(uploadResult.url))
                        )


                        sendMessageUseCase.execute(message, chatId)

                        _uploadResult.emit(UploadResult.Success(message))
                    }
                }
            }
        }
    }

    fun getMessages(chatId: String) {
        viewModelScope.launch {
            getMessagesByChatIdUseCase.execute(chatId)
                .distinctUntilChanged()
                .map { pagingData -> pagingData.map { it.toMessagesModel() } }
                .cachedIn(viewModelScope)
                .onEach { _messages.emit(it) }
                .catch { it.printStackTrace() }
                .launchIn(viewModelScope)
        }
    }

    fun subscribeMessages(chatId: String) {
        viewModelScope.launch(exceptionHandler) {
            subscribeOnChatMessagesUseCase.execute(chatId).collect {
                Timber.d("subscribeMessages: $it")
                prependNewMessage.execute(it, chatId)
            }
        }
    }

    fun getChatDataCachedInDb(chatId: String) {
        viewModelScope.launch(exceptionHandler) {
            val chat = getChatUseCase.execute(chatId)
            chat?.let {
                async {
                    chat.participantIds.keys.map {
                        getUsersCachedInDb(chat.participantIds.keys.toList())
                    }
                }.await()

                editScreenState(CurrentChat(it))
            }
        }
    }

    fun getMyProfile(): UserProfileModel {
        return userProfile.toUserProfileModel()
    }

    fun scrollToEnd() {
        viewModelScope.launch(exceptionHandler) {
            scrollState.scrollToItem(0)
        }
    }

    fun dropUnReadChatCounter(chatId: String) {
        viewModelScope.launch {
            dropUnReadMessagesCounterUseCase.execute(chatId)
        }
    }

    fun isPrevMessWasMine(
        messagesList: LazyPagingItems<MessageModel>,
        element: Int,
        message: MessageModel
    ): Boolean {
        return if (element == 0) {
            false
        } else if (messagesList[element - 1]?.sender == message.sender) {
            true
        } else {
            false
        }
    }

    fun isNextMessWillMine(
        messagesList: LazyPagingItems<MessageModel>,
        element: Int,
        message: MessageModel
    ): Boolean {
        return if (element + 1 == messagesList.itemCount) {
            false
        } else if (messagesList[element + 1]?.sender == message.sender) {
            true
        } else {
            false
        }
    }

    fun isDataHasChanged(
        messagesList: LazyPagingItems<MessageModel>,
        element: Int,
        message: MessageModel
    ): Long {

        val simpleDate = SimpleDateFormat(
            context.getString(R.string.date_format_dd_MMMM_yyyy),
            Locale.getDefault()
        )
        val currentMessageDate = simpleDate.format(Date(message.timestamp)).split(" ")

        if (element == messagesList.itemCount - 1) {
            return messagesList[element]?.timestamp ?: 0L
        } else if (element < messagesList.itemCount) {
            if (compareIfDateHasChanged(
                    currentMessageDate,
                    simpleDate.format(Date(messagesList[element + 1]?.timestamp ?: 0))
                        .split(" ")
                )
            ) {
                return messagesList[element]?.timestamp ?: 0L
            }
        }

        return 0L
    }
}

data class MessageScreenState(
    val collocutors: Collocutors = Collocutors(emptyList()),
    val hasInternet: HasInternet = HasInternet(true),
    val currentChat: CurrentChat = CurrentChat(ChatModel()),
    val imagePreview: ImagePreview = ImagePreview(),
)

sealed class ScreenStateParams {
    data class Collocutors(val value: List<UserModel> = emptyList()) : ScreenStateParams()
    data class HasInternet(val value: Boolean = true) : ScreenStateParams()
    data class CurrentChat(val value: ChatModel = ChatModel()) : ScreenStateParams()
    data class ImagePreview(val value: String = "") : ScreenStateParams()
}

sealed class UploadResult {
    data class Loading(val percentage: Int) : UploadResult()
    data class Error(val message: String) : UploadResult()
    data class Success(val messageModel: MessageModel) : UploadResult()
}