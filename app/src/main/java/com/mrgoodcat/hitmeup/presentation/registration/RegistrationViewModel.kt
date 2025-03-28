package com.mrgoodcat.hitmeup.presentation.registration

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.di.ApplicationScope
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.Status
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_ERROR_INVALID_EMAIL
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_ERROR_TOO_MUCH_ATTEMPTS
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_ERROR_USER_NOT_FOUND
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_ERROR_WRONG_PASSWORD
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_UNKNOWN_ERROR
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.GLOBAL_ERROR_UNKNOWN_ERROR
import com.mrgoodcat.hitmeup.domain.usecase.UpdateFcmTokenUseCase
import com.mrgoodcat.hitmeup.domain.usecase.UpdateUserProfileFromApiUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.CheckIsEmailAlreadyExistsEmailUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.CheckIsEmailAlreadyExistsFacebookUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.CheckIsEmailAlreadyExistsGoogleUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.CreateUserEmailUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.CreateUserSocialUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.ReleaseAfterAuthorizationUseCase
import com.mrgoodcat.hitmeup.domain.usecase.registration.SendVerificationEmailUseCase
import com.mrgoodcat.hitmeup.domain.utils.TextUtils
import com.mrgoodcat.hitmeup.presentation.getStateFlow
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.ExistUserDialogState
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.ExistedUser
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.HasInternet
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.IsLoading
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.LoginStateError
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.LoginString
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.LoginStringError
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.PasswordStateError
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.PasswordStateErrorRepeat
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.PasswordString
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.PasswordStringError
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.PasswordStringErrorRepeat
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.PasswordStringRepeat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val appScope: CoroutineScope,
    private val checkIsEmailAlreadyExistsGoogleUseCase: CheckIsEmailAlreadyExistsGoogleUseCase,
    private val checkIsEmailAlreadyExistsFacebookUseCase: CheckIsEmailAlreadyExistsFacebookUseCase,
    private val checkIsEmailAlreadyExistsEmailUseCase: CheckIsEmailAlreadyExistsEmailUseCase,
    private val updateUserProfileFromApiUseCase: UpdateUserProfileFromApiUseCase,
    private val createUserSocialUseCase: CreateUserSocialUseCase,
    private val createUserEmailUseCase: CreateUserEmailUseCase,
    private val releaseAfterAuthorizationUseCase: ReleaseAfterAuthorizationUseCase,
    private val connectivityManager: ConnectivityStateManager,
    private val savedStateHandle: SavedStateHandle,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase,
    private val sendVerificationEmailUseCase: SendVerificationEmailUseCase,
    private val textUtils: TextUtils,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private val _registerResult: MutableStateFlow<CreateUserResult?> =
        savedStateHandle.getStateFlow(
            appScope,
            CREATE_USER_KEY,
            null
        )
    val registerResult = _registerResult.asStateFlow()

    private val _registerScreenState: MutableStateFlow<RegisterUserState> =
        savedStateHandle.getStateFlow(
            appScope,
            SCREEN_STATE_KEY,
            RegisterUserState()
        )
    val registerScreenState = _registerScreenState.asStateFlow()

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
                                Constants.ANALYTICS_EVENT_OPEN_SCREEN_REGISTRATION
                    )
                )
        }
    }

    private suspend fun createUser(email: String, password: String): CreateUserResult {
        Timber.d("createUser: $email $password")
        return createUserEmailUseCase.execute(email, password)
    }

    private suspend fun checkAuthForGoogleUser(context: WeakReference<Activity>): CheckIsUserExists {
        return checkIsEmailAlreadyExistsGoogleUseCase.execute(context)
    }

    private suspend fun checkAuthForFacebookUser(context: WeakReference<ActivityResultRegistryOwner>): CheckIsUserExists {
        return checkIsEmailAlreadyExistsFacebookUseCase.execute(context)
    }

    private suspend fun checkAuthForEmailUser(email: String, password: String): CheckIsUserExists {
        return checkIsEmailAlreadyExistsEmailUseCase.execute(email, password)
    }

    private suspend fun createUser(user: FirebaseUser?): CreateUserResult {
        Timber.d("createUser: $user")
        if (user == null) return CreateUserResult.Error(GLOBAL_ERROR_UNKNOWN_ERROR)
        return createUserSocialUseCase.execute(user)
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
                            Timber.d("status $it")
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

    fun releaseUserAuthorization() {
        viewModelScope.launch(exceptionHandler) {
            releaseAfterAuthorizationUseCase.execute()
        }
    }

    fun validateAndRegisterWithEmail() {
        viewModelScope.launch(exceptionHandler) {
            val screenState = _registerScreenState.value
            var counter = 0

            when (val res = textUtils.validateEmail(screenState.loginString.value)) {
                is TextUtils.ValidateResult.IsNotOk -> {
                    editScreenState(LoginStateError(true))
                    editScreenState(LoginStringError(res.errorCode))
                    counter++
                }

                is TextUtils.ValidateResult.IsOk -> {
                    editScreenState(LoginStateError(false))
                    editScreenState(LoginStringError())
                }
            }

            when (val res = textUtils.validatePassword(
                password = screenState.passwordString.value
            )) {
                is TextUtils.ValidateResult.IsNotOk -> {
                    editScreenState(PasswordStateError(true))
                    editScreenState(PasswordStringError(res.errorCode))
                    counter++
                }

                is TextUtils.ValidateResult.IsOk -> {
                    editScreenState(PasswordStateError(false))
                    editScreenState(PasswordStringError())
                }
            }

            when (val res = textUtils.validateRepeatedPassword(
                password = screenState.passwordString.value,
                passwordRepeated = screenState.passwordStringRepeat.value
            )) {
                is TextUtils.ValidateResult.IsNotOk -> {
                    editScreenState(PasswordStateErrorRepeat(true))
                    editScreenState(PasswordStringErrorRepeat(res.errorCode))
                    counter++
                }

                is TextUtils.ValidateResult.IsOk -> {
                    editScreenState(PasswordStateErrorRepeat(false))
                    editScreenState(PasswordStringErrorRepeat())
                }
            }

            if (counter > 0) {
                return@launch
            }

            editScreenState(IsLoading(true))

            val isExists = checkAuthForEmailUser(
                screenState.loginString.value,
                screenState.passwordString.value
            )

            if (isExists is CheckIsUserExists.IsExists) {
                Timber.d("CheckIsUserExists.IsExists")
                val newState = _registerScreenState.value.copy(
                    existUserDialogState = ExistUserDialogState(true),
                    existedUser = ExistedUser(isExists.user),
                    isLoading = IsLoading(false)
                )

                _registerScreenState.emit(newState)
                return@launch
            }

            if (isExists is CheckIsUserExists.Error) {
                Timber.d("CheckIsUserExists.Error ${isExists.message}")
                when (isExists.message) {
                    FIREBASE_ERROR_INVALID_EMAIL -> {
                        val newState = _registerScreenState.value.copy(
                            loginErrorString = LoginStringError(context.getString(R.string.email_wrong_format_error)),
                            loginErrorState = LoginStateError(true),
                            isLoading = IsLoading(false)
                        )

                        _registerScreenState.emit(newState)
                    }

                    FIREBASE_ERROR_USER_NOT_FOUND -> {
                        val result = createUser(
                            screenState.loginString.value,
                            screenState.passwordString.value
                        )

                        when (result) {
                            is CreateUserResult.Error -> {
                                val newState = _registerScreenState.value.copy(
                                    loginErrorString = LoginStringError(result.errorMessage),
                                    loginErrorState = LoginStateError(true),
                                    isLoading = IsLoading(false)
                                )

                                _registerScreenState.emit(newState)
                            }

                            is CreateUserResult.Success -> {
                                updateUserProfileFromApiUseCase.execute(result.user.user_id)
                                updateFcmToken(result.user.user_id)
                                _registerResult.emit(result)
                                sendVerificationEmail()
                            }
                        }
                    }

                    FIREBASE_ERROR_WRONG_PASSWORD -> {
                        Timber.d("CheckIsUserExists.Error enter in FIREBASE_ERROR_WRONG_PASSWORD")
                        releaseUserAuthorization()
                        val newState = _registerScreenState.value.copy(
                            loginErrorString = LoginStringError(context.getString(R.string.email_exists_error)),
                            loginErrorState = LoginStateError(true),
                            isLoading = IsLoading(false)
                        )

                        _registerScreenState.emit(newState)
                    }

                    FIREBASE_UNKNOWN_ERROR -> {
                        releaseUserAuthorization()
                        val newState = _registerScreenState.value.copy(
                            passwordStringErrorRepeat = PasswordStringErrorRepeat(
                                context.getString(
                                    R.string.load_data_error
                                )
                            ),
                            passwordStateErrorRepeat = PasswordStateErrorRepeat(true),
                            isLoading = IsLoading(false)
                        )

                        _registerScreenState.emit(newState)
                    }

                    FIREBASE_ERROR_TOO_MUCH_ATTEMPTS -> {
                        releaseUserAuthorization()
                        val newState = _registerScreenState.value.copy(
                            passwordStringErrorRepeat = PasswordStringErrorRepeat(
                                context.getString(
                                    R.string.too_much_attempts
                                )
                            ),
                            passwordStateErrorRepeat = PasswordStateErrorRepeat(true),
                            isLoading = IsLoading(false)
                        )

                        _registerScreenState.emit(newState)
                    }
                }
            }
            editScreenState(IsLoading(false))
        }
    }

    fun registerUserWithGoogle(context: WeakReference<Activity>) {
        viewModelScope.launch(exceptionHandler) {

            editScreenState(IsLoading(true))

            val isExists = checkAuthForGoogleUser(context)

            when (isExists) {
                is CheckIsUserExists.Error -> {
                    editScreenState(IsLoading(false))
                }

                is CheckIsUserExists.IsExists -> {
                    val newState = _registerScreenState.value.copy(
                        existedUser = ExistedUser(isExists.user),
                        existUserDialogState = ExistUserDialogState(true),
                        isLoading = IsLoading(false)
                    )

                    _registerScreenState.emit(newState)
                }

                is CheckIsUserExists.IsNotExists -> {
                    val result = createUser(isExists.user)

                    if (result is CreateUserResult.Success) {
                        updateUserProfileFromApiUseCase.execute(result.user.user_id)
                        updateFcmToken(result.user.user_id)
                        sendVerificationEmail()
                    }

                    _registerResult.emit(result)
                    editScreenState(IsLoading(false))
                }

                is CheckIsUserExists.Loading -> {}
            }

            Timber.d("RegistrationScreen: isExists $isExists")
        }
    }

    fun registerUserWithFacebook(context: WeakReference<ActivityResultRegistryOwner>) {
        viewModelScope.launch(exceptionHandler) {
            editScreenState(IsLoading(true))

            val isExists = checkAuthForFacebookUser(context)

            when (isExists) {
                is CheckIsUserExists.Error -> {
                    editScreenState(IsLoading(false))
                }

                is CheckIsUserExists.IsExists -> {
                    val newState = _registerScreenState.value.copy(
                        existedUser = ExistedUser(isExists.user),
                        existUserDialogState = ExistUserDialogState(true),
                        isLoading = IsLoading(false)
                    )

                    _registerScreenState.emit(newState)
                }

                is CheckIsUserExists.IsNotExists -> {
                    val result = createUser(isExists.user)

                    if (result is CreateUserResult.Success) {
                        updateUserProfileFromApiUseCase.execute(result.user.user_id)
                        updateFcmToken(result.user.user_id)
                        sendVerificationEmail()
                    }

                    _registerResult.emit(result)
                    editScreenState(IsLoading(false))
                }

                is CheckIsUserExists.Loading -> {}
            }

            Timber.d("RegistrationScreen: isExists $isExists")
        }
    }

    fun editScreenState(params: StateParams) {
        viewModelScope.launch(exceptionHandler) {
            when (params) {
                is HasInternet -> {
                    val newVal = _registerScreenState.value.copy(hasInternet = params)
                    _registerScreenState.emit(newVal)
                }

                is LoginString -> {
                    val newVal = _registerScreenState.value.copy(loginString = params)
                    _registerScreenState.emit(newVal)
                }

                is PasswordString -> {
                    val newVal = _registerScreenState.value.copy(passwordString = params)
                    _registerScreenState.emit(newVal)
                }

                is LoginStateError -> {
                    val newVal = _registerScreenState.value.copy(loginErrorState = params)
                    _registerScreenState.emit(newVal)
                }

                is LoginStringError -> {
                    val newVal = _registerScreenState.value.copy(loginErrorString = params)
                    _registerScreenState.emit(newVal)
                }

                is PasswordStateError -> {
                    val newVal = _registerScreenState.value.copy(passwordStateError = params)
                    _registerScreenState.emit(newVal)
                }

                is PasswordStringError -> {
                    val newVal = _registerScreenState.value.copy(passwordStringError = params)
                    _registerScreenState.emit(newVal)
                }

                is ExistUserDialogState -> {
                    val newVal = _registerScreenState.value.copy(existUserDialogState = params)
                    _registerScreenState.emit(newVal)
                }

                is ExistedUser -> {
                    val newVal = _registerScreenState.value.copy(existedUser = params)
                    _registerScreenState.emit(newVal)
                }

                is PasswordStateErrorRepeat -> {
                    val newVal = _registerScreenState.value.copy(passwordStateErrorRepeat = params)
                    _registerScreenState.emit(newVal)
                }

                is PasswordStringErrorRepeat -> {
                    val newVal = _registerScreenState.value.copy(passwordStringErrorRepeat = params)
                    _registerScreenState.emit(newVal)
                }

                is PasswordStringRepeat -> {
                    val newVal = _registerScreenState.value.copy(passwordStringRepeat = params)
                    _registerScreenState.emit(newVal)
                }

                is IsLoading -> {
                    val newVal = _registerScreenState.value.copy(isLoading = params)
                    _registerScreenState.emit(newVal)
                }
            }
        }
    }

    fun updateNetworkStatus() {
        editScreenState(HasInternet(connectivityManager.isOnline()))
    }

    private fun sendVerificationEmail() {
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

    fun updateFcmToken(userId: String) {
        viewModelScope.launch(exceptionHandler) {
            updateFcmTokenUseCase.execute(userId)
        }
    }

    companion object {
        private const val CREATE_USER_KEY = "create_user_key"
        private const val SCREEN_STATE_KEY = "screen_state_key"
    }
}

@Parcelize
data class RegisterUserState(
    val loginString: LoginString = LoginString(),
    val loginErrorString: LoginStringError = LoginStringError(),
    val loginErrorState: LoginStateError = LoginStateError(),
    val passwordString: PasswordString = PasswordString(),
    val passwordStringError: PasswordStringError = PasswordStringError(),
    val passwordStateError: PasswordStateError = PasswordStateError(),
    val existedUser: ExistedUser = ExistedUser(),
    val existUserDialogState: ExistUserDialogState = ExistUserDialogState(),
    val passwordStringRepeat: PasswordStringRepeat = PasswordStringRepeat(),
    val passwordStringErrorRepeat: PasswordStringErrorRepeat = PasswordStringErrorRepeat(),
    val passwordStateErrorRepeat: PasswordStateErrorRepeat = PasswordStateErrorRepeat(),
    val hasInternet: HasInternet = HasInternet(),
    val isLoading: IsLoading = IsLoading()
) : Parcelable

@Parcelize
sealed class StateParams : Parcelable {
    data class HasInternet(val value: Boolean = true) : StateParams()
    data class ExistedUser(val value: UserLocalModel = UserLocalModel()) : StateParams()
    data class ExistUserDialogState(val value: Boolean = false) : StateParams()
    data class IsLoading(val value: Boolean = false) : StateParams()

    data class LoginString(val value: String = "") : StateParams()
    data class LoginStringError(val value: String = "") : StateParams()
    data class LoginStateError(val value: Boolean = false) : StateParams()

    data class PasswordString(val value: String = "") : StateParams()
    data class PasswordStringError(val value: String = "") : StateParams()
    data class PasswordStateError(val value: Boolean = false) : StateParams()

    data class PasswordStringRepeat(val value: String = "") : StateParams()
    data class PasswordStringErrorRepeat(val value: String = "") : StateParams()
    data class PasswordStateErrorRepeat(val value: Boolean = false) : StateParams()
}

@Parcelize
sealed class CheckIsUserExists : Parcelable {
    data class Loading(val isLoading: Boolean) : CheckIsUserExists()
    data class IsExists(val user: UserLocalModel) : CheckIsUserExists()
    data class IsNotExists(val user: FirebaseUser?) : CheckIsUserExists()
    data class Error(val message: String = "") : CheckIsUserExists()
}

@Parcelize
sealed class CreateUserResult : Parcelable {
    data class Success(val user: UserLocalModel) : CreateUserResult()
    data class Error(val errorMessage: String) : CreateUserResult()
}