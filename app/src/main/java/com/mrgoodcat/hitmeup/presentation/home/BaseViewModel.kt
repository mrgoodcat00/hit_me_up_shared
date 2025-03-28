package com.mrgoodcat.hitmeup.presentation.home

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.MessagesScreen
import com.mrgoodcat.hitmeup.domain.usecase.RegisterPushBroadcastReceiverUseCase
import com.mrgoodcat.hitmeup.domain.usecase.UpdateLastSeenUseCase
import com.mrgoodcat.hitmeup.domain.usecase.UpdateUserProfileFromApiUseCase
import com.mrgoodcat.hitmeup.domain.usecase.chats.SubscribeOnChatListChangesUseCase
import com.mrgoodcat.hitmeup.domain.usecase.login.IsUserVerifiedUseCase
import com.mrgoodcat.hitmeup.domain.usecase.messages.CreateNotificationUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.ReleaseAfterAuthorizationUseCase
import com.mrgoodcat.hitmeup.presentation.MainActivity.Companion.ARGUMENT_NAV_CHAT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BaseViewModel @Inject constructor(
    private val updateUserProfileFromApiUseCase: UpdateUserProfileFromApiUseCase,
    private val checkAuthorizationInUserApiUseCase: CheckAuthorizationInUserApiUseCase,
    private val releaseAfterAuthorizationUseCase: ReleaseAfterAuthorizationUseCase,
    private val subscribeOnChatListChangesUseCase: SubscribeOnChatListChangesUseCase,
    private val updateCurrentScreenUseCase: UpdateCurrentScreenUseCase,
    private val registerPushBroadcastReceiverUseCase: RegisterPushBroadcastReceiverUseCase,
    private val createNotificationUseCase: CreateNotificationUseCase,
    private val isUserVerifiedUseCase: IsUserVerifiedUseCase,
    private val updateLastSeenUseCase: UpdateLastSeenUseCase,
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private val _authorizationFlag = MutableSharedFlow<AuthState>()
    val authorizationFlag = _authorizationFlag.asSharedFlow()

    private val _keepSplashScreen = MutableStateFlow(true)
    val splashAnimationDone = _keepSplashScreen.asStateFlow()

    private val authStateListener = AuthStateListener { firebaseAuthState ->
        viewModelScope.launch(exceptionHandler) {
            Timber.d("BaseViewModel auth currentUser: ${firebaseAuthState.currentUser}")

            if (firebaseAuthState.currentUser == null) {
                _authorizationFlag.emit(AuthState.LoggedIn(isLoggedIn = false))
                _keepSplashScreen.emit(false)
            } else {

                val uId = firebaseAuthState.currentUser?.uid
                val userIsVerified = firebaseAuthState.currentUser?.isEmailVerified
                val userHasEmail = firebaseAuthState.currentUser?.email?.isNotEmpty()


                if (uId == null) {
                    _authorizationFlag.emit(AuthState.LoggedIn(isLoggedIn = false))
                    releaseAfterAuthorizationUseCase.execute()
                    _keepSplashScreen.emit(false)
                    return@launch
                }

                if (checkAuthorizationInUserApiUseCase.execute(uId)) {
                    _authorizationFlag.emit(AuthState.LoggedIn(isLoggedIn = false))
                    releaseAfterAuthorizationUseCase.execute()
                    _keepSplashScreen.emit(false)
                    return@launch
                }

                if (userIsVerified != true && userHasEmail == true) {
                    isUserVerifiedUseCase.execute(false)
                }

                _keepSplashScreen.emit(false)
                updateUserProfileFromApiUseCase.execute(uId)
                updateLastSeenUseCase.execute(uId)
                _authorizationFlag.emit(AuthState.LoggedIn(isLoggedIn = true))
                subscribeMessages()
                subscribeOnPushNotifications()
            }
        }
    }


    init {
        Timber.d("baseViewModel init")
        addAuthListener()
    }

    fun getListener(): AuthStateListener {
        return authStateListener
    }

    fun addAuthListener() {
        Timber.d("addAuthStateListener ${FirebaseAuth.getInstance().pendingAuthResult?.result}")
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    fun removeAuthListener() {
        Timber.d("removeAuthStateListener")
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }

    fun updateCurrentScreenName(navBackStackEntry: NavBackStackEntry) {
        viewModelScope.launch(exceptionHandler) {
            if (navBackStackEntry.destination.route == "${MessagesScreen.route}/{${ARGUMENT_NAV_CHAT_ID}}") {
                navBackStackEntry.arguments?.getString(ARGUMENT_NAV_CHAT_ID).let {
                    updateCurrentScreenUseCase.execute(
                        navBackStackEntry.destination.route ?: "",
                        it ?: ""
                    )
                }
            } else {
                updateCurrentScreenUseCase.execute(navBackStackEntry.destination.route ?: "", "")
            }
        }
    }

    private fun subscribeMessages() {
        viewModelScope.launch(exceptionHandler) {
            subscribeOnChatListChangesUseCase.execute()
        }
    }

    private fun subscribeOnPushNotifications() {
        viewModelScope.launch(exceptionHandler) {
            registerPushBroadcastReceiverUseCase.execute()
                .collect { pushModel ->
                    createNotificationUseCase.execute(pushModel)
                }
        }
    }

    override fun onCleared() {
        removeAuthListener()
        super.onCleared()
    }
}

@Stable
sealed class AuthState {
    data class LoggedIn(val message: String = "", val isLoggedIn: Boolean) : AuthState()
    data class Error(val message: String = "", val isLoggedIn: Boolean) : AuthState()
}