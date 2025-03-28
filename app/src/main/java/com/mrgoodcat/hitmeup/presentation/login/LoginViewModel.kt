package com.mrgoodcat.hitmeup.presentation.login

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.di.ApplicationScope
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.Status
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.GLOBAL_ERROR_UNKNOWN_ERROR
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.GLOBAL_ERROR_USER_DOESNT_EXIST_IN_DB
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult.IsLoading
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult.ResultFailed
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult.ResultFalse
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult.ResultSuccessful
import com.mrgoodcat.hitmeup.domain.usecase.UpdateFcmTokenUseCase
import com.mrgoodcat.hitmeup.domain.usecase.UpdateUserProfileFromApiUseCase
import com.mrgoodcat.hitmeup.domain.usecase.login.AuthorizeWithEmailUseCase
import com.mrgoodcat.hitmeup.domain.usecase.login.AuthorizeWithFacebookUseCase
import com.mrgoodcat.hitmeup.domain.usecase.login.AuthorizeWithGoogleUseCase
import com.mrgoodcat.hitmeup.domain.usecase.login.CheckAuthorizationInUserApiUseCase
import com.mrgoodcat.hitmeup.domain.utils.TextUtils
import com.mrgoodcat.hitmeup.presentation.getStateFlow
import com.mrgoodcat.hitmeup.presentation.login.StateParams.HasInternet
import com.mrgoodcat.hitmeup.presentation.login.StateParams.LoginStateError
import com.mrgoodcat.hitmeup.presentation.login.StateParams.LoginString
import com.mrgoodcat.hitmeup.presentation.login.StateParams.LoginStringError
import com.mrgoodcat.hitmeup.presentation.login.StateParams.PasswordStateError
import com.mrgoodcat.hitmeup.presentation.login.StateParams.PasswordString
import com.mrgoodcat.hitmeup.presentation.login.StateParams.PasswordStringError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Serializable
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val appScope: CoroutineScope,
    private val authorizeWithGoogleUseCase: AuthorizeWithGoogleUseCase,
    private val authorizeWithFacebookUseCase: AuthorizeWithFacebookUseCase,
    private val authorizeWithEmailUseCase: AuthorizeWithEmailUseCase,
    private val updateUserProfileFromApiUseCase: UpdateUserProfileFromApiUseCase,
    private val checkAuthorizationInUserApiUseCase: CheckAuthorizationInUserApiUseCase,
    private val connectivityManager: ConnectivityStateManager,
    private val savedStateHandle: SavedStateHandle,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase,
    private val textUtils: TextUtils,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private var _authorizationResult = MutableSharedFlow<AuthorizationResult>()
    val authorizationResult = _authorizationResult.asSharedFlow()

    private val _loginState: MutableStateFlow<LoginScreenState> =
        savedStateHandle.getStateFlow(
            appScope,
            LOGIN_SCREEN_STATE_KEY,
            LoginScreenState()
        )
    val loginState = _loginState.asStateFlow()

    init {
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
                                Constants.ANALYTICS_EVENT_OPEN_SCREEN_LOGIN
                    )
                )
        }
    }

    private fun authorizeWithEmail(login: String, pass: String) {
        viewModelScope.launch(exceptionHandler) {
            _authorizationResult.emit(IsLoading)

            val firebaseResult = authorizeWithEmailUseCase.execute(login, pass)

            if (firebaseResult !is ResultSuccessful) {
                _authorizationResult.emit(firebaseResult)
                return@launch
            }

            val userId = firebaseResult.user?.uid

            if (userId == null) {
                _authorizationResult.emit(ResultFailed(GLOBAL_ERROR_UNKNOWN_ERROR))
                return@launch
            }

            if (checkAuthorizationInUserApiUseCase.execute(userId)) {
                _authorizationResult.emit(ResultFalse(GLOBAL_ERROR_USER_DOESNT_EXIST_IN_DB))
                return@launch
            }

            updateFcmTokenUseCase.execute(userId)
            updateUserProfileFromApiUseCase.execute(userId)
            _authorizationResult.emit(ResultSuccessful(firebaseResult.user))
        }
    }

    private fun subscribeOnNetworkStatus() {
        viewModelScope.launch(exceptionHandler) {
            connectivityManager
                .observeNetworkStatus()
                .collect {
                    when (it) {
                        Status.Available -> {
                            editScreenState(
                                HasInternet(
                                    true
                                )
                            )
                        }

                        else -> {
                            editScreenState(
                                HasInternet(
                                    false
                                )
                            )
                        }
                    }
                }
        }
    }

    fun authorizeWithGoogle(context: WeakReference<Activity>) {
        viewModelScope.launch(exceptionHandler) {
            _authorizationResult.emit(IsLoading)

            val firebaseResult = authorizeWithGoogleUseCase.execute(context)

            if (firebaseResult !is ResultSuccessful) {
                _authorizationResult.emit(firebaseResult)
                return@launch
            }

            val userId = firebaseResult.user?.uid

            if (userId == null) {
                _authorizationResult.emit(ResultFailed(GLOBAL_ERROR_UNKNOWN_ERROR))
                return@launch
            }

            if (checkAuthorizationInUserApiUseCase.execute(userId)) {
                _authorizationResult.emit(ResultFailed(GLOBAL_ERROR_USER_DOESNT_EXIST_IN_DB))
                return@launch
            }

            updateFcmTokenUseCase.execute(userId)
            updateUserProfileFromApiUseCase.execute(userId)
            _authorizationResult.emit(ResultSuccessful(firebaseResult.user))
        }
    }

    fun authorizeWithFacebook(context: WeakReference<ActivityResultRegistryOwner>) {
        viewModelScope.launch(exceptionHandler) {
            _authorizationResult.emit(IsLoading)

            val firebaseResult = authorizeWithFacebookUseCase.execute(context)

            if (firebaseResult !is ResultSuccessful) {
                _authorizationResult.emit(firebaseResult)
                return@launch
            }

            val userId = firebaseResult.user?.uid

            if (userId == null) {
                _authorizationResult.emit(ResultFailed(GLOBAL_ERROR_UNKNOWN_ERROR))
                return@launch
            }

            if (checkAuthorizationInUserApiUseCase.execute(userId)) {
                _authorizationResult.emit(ResultFailed(GLOBAL_ERROR_USER_DOESNT_EXIST_IN_DB))
                return@launch
            }

            updateFcmTokenUseCase.execute(userId)
            updateUserProfileFromApiUseCase.execute(userId)
            _authorizationResult.emit(ResultSuccessful(firebaseResult.user))
        }
    }

    fun authorizeWithEmail() {
        var error = 0

        when (val res = textUtils.validateEmail(_loginState.value.loginString.value)) {
            is TextUtils.ValidateResult.IsNotOk -> {
                _loginState.value = _loginState.value.copy(
                    loginStateError = LoginStateError(true),
                    loginStringError = LoginStringError(res.errorCode)
                )
                error++
            }

            is TextUtils.ValidateResult.IsOk -> {
                _loginState.value = _loginState.value.copy(
                    loginStateError = LoginStateError(false),
                    loginStringError = LoginStringError()
                )
            }
        }

        when (val res = textUtils.validatePassword(
            password = _loginState.value.passwordString.value
        )) {
            is TextUtils.ValidateResult.IsNotOk -> {
                _loginState.value = _loginState.value.copy(
                    passwordStateError = PasswordStateError(true),
                    passwordStringError = PasswordStringError(res.errorCode)
                )
                error++
            }

            is TextUtils.ValidateResult.IsOk -> {
                _loginState.value = _loginState.value.copy(
                    passwordStateError = PasswordStateError(),
                    passwordStringError = PasswordStringError()
                )
            }
        }

        if (error > 0) {
            return
        }

        viewModelScope.launch(exceptionHandler) {
            _authorizationResult.emit(IsLoading)
        }

        authorizeWithEmail(
            loginState.value.loginString.value,
            loginState.value.passwordString.value
        )
    }

    fun editScreenState(params: StateParams) {
        viewModelScope.launch(exceptionHandler) {
            when (params) {
                is HasInternet -> {
                    val newVal = _loginState.value.copy(hasInternet = params)
                    _loginState.emit(newVal)
                }

                is LoginString -> {
                    val newVal = _loginState.value.copy(loginString = params)
                    _loginState.emit(newVal)
                }

                is PasswordString -> {
                    val newVal = _loginState.value.copy(passwordString = params)
                    _loginState.emit(newVal)
                }

                is LoginStateError -> {
                    val newVal = _loginState.value.copy(loginStateError = params)
                    _loginState.emit(newVal)
                }

                is LoginStringError -> {
                    val newVal = _loginState.value.copy(loginStringError = params)
                    _loginState.emit(newVal)
                }

                is PasswordStateError -> {
                    val newVal = _loginState.value.copy(passwordStateError = params)
                    _loginState.emit(newVal)
                }

                is PasswordStringError -> {
                    val newVal = _loginState.value.copy(passwordStringError = params)
                    _loginState.emit(newVal)
                }
            }
        }
    }

    fun updateNetworkStatus() {
        editScreenState(HasInternet(connectivityManager.isOnline()))
    }

    companion object {
        private const val LOGIN_SCREEN_STATE_KEY = "login_screen_state_key"
    }
}

data class LoginScreenState(
    val hasInternet: HasInternet = HasInternet(),
    val loginString: LoginString = LoginString(),
    val passwordString: PasswordString = PasswordString(),
    val loginStringError: LoginStringError = LoginStringError(),
    val passwordStringError: PasswordStringError = PasswordStringError(),
    val loginStateError: LoginStateError = LoginStateError(),
    val passwordStateError: PasswordStateError = PasswordStateError(),
) : Serializable

sealed class StateParams : Serializable {
    data class HasInternet(val value: Boolean = true) : StateParams()
    data class LoginString(val value: String = "") : StateParams()
    data class PasswordString(val value: String = "") : StateParams()
    data class LoginStringError(val value: String = "") : StateParams()
    data class PasswordStringError(val value: String = "") : StateParams()
    data class LoginStateError(val value: Boolean = false) : StateParams()
    data class PasswordStateError(val value: Boolean = false) : StateParams()
}