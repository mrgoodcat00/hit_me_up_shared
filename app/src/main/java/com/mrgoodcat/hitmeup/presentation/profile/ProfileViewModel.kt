package com.mrgoodcat.hitmeup.presentation.profile

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.Status
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.model.UserProfileModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserProfileModel
import com.mrgoodcat.hitmeup.domain.usecase.UpdateFcmTokenUseCase
import com.mrgoodcat.hitmeup.domain.usecase.UpdateLastSeenUseCase
import com.mrgoodcat.hitmeup.domain.usecase.profile.ClearUserDataUseCase
import com.mrgoodcat.hitmeup.domain.usecase.profile.DeleteUserUseCase
import com.mrgoodcat.hitmeup.domain.usecase.profile.GetMyProfileUseCase
import com.mrgoodcat.hitmeup.domain.usecase.profile.LogoutUseCase
import com.mrgoodcat.hitmeup.presentation.profile.StateParams.HasInternet
import com.mrgoodcat.hitmeup.presentation.profile.StateParams.IsDeleteDialog
import com.mrgoodcat.hitmeup.presentation.profile.StateParams.IsLoading
import com.mrgoodcat.hitmeup.presentation.profile.StateParams.LoggedOut
import com.mrgoodcat.hitmeup.presentation.profile.StateParams.UserModelReady
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val connectivityManager: ConnectivityStateManager,
    private val logoutUseCase: LogoutUseCase,
    private val clearUserDataUseCase: ClearUserDataUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val updateLastSeenUseCase: UpdateLastSeenUseCase,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,
) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
        editScreenState(IsDeleteDialog())
        editScreenState(IsLoading())
    }

    private var _profileScreenState = MutableStateFlow(ProfileScreenState())
    val profileScreenState = _profileScreenState.asStateFlow()

    init {
        subscribeOnNetworkStatus()
        logAnalytics()
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

    private fun logAnalytics() {
        viewModelScope.launch(exceptionHandler) {
            hitMeUpFirebaseAnalytics
                .getAnalytics()
                .logEvent(
                    FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
                        FirebaseAnalytics.Param.SCREEN_NAME to
                                Constants.ANALYTICS_EVENT_OPEN_SCREEN_USER_PROFILE
                    )
                )
        }
    }

    fun logoutUser() {
        viewModelScope.launch(exceptionHandler) {
            updateLastSeenUseCase.execute(profileScreenState.value.userModel.value.userId)
            updateFcmTokenUseCase.execute(profileScreenState.value.userModel.value.userId, true)
            clearUserDataUseCase.execute()
            logoutUseCase.execute()
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch(exceptionHandler) {
            editScreenState(IsLoading(true))

            val profileModel = getMyProfileUseCase.execute().toUserProfileModel()
            editScreenState(UserModelReady(profileModel))
            editScreenState(IsLoading(false))
        }
    }

    fun updateNetworkStatus() {
        editScreenState(HasInternet(connectivityManager.isOnline()))
    }

    fun editScreenState(params: StateParams) {
        viewModelScope.launch(exceptionHandler) {
            when (params) {
                is HasInternet -> {
                    val newVal = _profileScreenState.value.copy(hasInternet = params)
                    _profileScreenState.emit(newVal)
                }

                is IsLoading -> {
                    val newVal = _profileScreenState.value.copy(loading = params)
                    _profileScreenState.emit(newVal)
                }

                is LoggedOut -> {
                    val newVal = _profileScreenState.value.copy(isLogout = params)
                    _profileScreenState.emit(newVal)
                }

                is UserModelReady -> {
                    val newVal = _profileScreenState.value.copy(userModel = params)
                    _profileScreenState.emit(newVal)
                }

                is IsDeleteDialog -> {
                    val newVal = _profileScreenState.value.copy(isDeleteDialog = params)
                    _profileScreenState.emit(newVal)
                }
            }
        }
    }

    fun deleteUser() {
        viewModelScope.launch(exceptionHandler) {
            deleteUserUseCase.execute(profileScreenState.value.userModel.value.userId)
            editScreenState(IsDeleteDialog())
            editScreenState(IsLoading())
        }
    }
}

@Parcelize
data class ProfileScreenState(
    val hasInternet: HasInternet = HasInternet(true),
    val loading: IsLoading = IsLoading(false),
    val userModel: UserModelReady = UserModelReady(UserProfileModel()),
    val isLogout: LoggedOut = LoggedOut(false),
    val isDeleteDialog: IsDeleteDialog = IsDeleteDialog(false)
) : Parcelable

@Parcelize
sealed class StateParams : Parcelable {
    data class HasInternet(val value: Boolean = true) : StateParams()
    data class IsLoading(val value: Boolean = false) : StateParams()
    data class UserModelReady(val value: UserProfileModel = UserProfileModel()) : StateParams()
    data class LoggedOut(val value: Boolean = false) : StateParams()
    data class IsDeleteDialog(val value: Boolean = false) : StateParams()
}