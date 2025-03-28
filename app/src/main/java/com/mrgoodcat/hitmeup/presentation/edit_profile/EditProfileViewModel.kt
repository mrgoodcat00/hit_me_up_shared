package com.mrgoodcat.hitmeup.presentation.edit_profile

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.data.repostory.UploadMediaResult
import com.mrgoodcat.hitmeup.di.ApplicationScope
import com.mrgoodcat.hitmeup.domain.ConnectivityStateManager
import com.mrgoodcat.hitmeup.domain.Status
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserProfileModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.usecase.edit_profile.GetMyProfileUseCase
import com.mrgoodcat.hitmeup.domain.usecase.edit_profile.UpdateMyProfileUseCase
import com.mrgoodcat.hitmeup.domain.usecase.edit_profile.UploadImageUseCase
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.AvatarString
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.AvatarUri
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.Email
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.FirstName
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.FirstNameError
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.HasInternet
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.LastName
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.LastNameError
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.Phone
import com.mrgoodcat.hitmeup.presentation.edit_profile.StateParams.PhoneError
import com.mrgoodcat.hitmeup.presentation.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val connectivityManager: ConnectivityStateManager,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,
    private val dbRepository: DbRepository,
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private val _currentUser: MutableStateFlow<EditProfileScreeState> =
        savedStateHandle.getStateFlow(
            applicationScope,
            EDIT_PROFILE_KEY,
            EditProfileScreeState()
        )

    val currentUser: StateFlow<EditProfileScreeState> = _currentUser.asStateFlow()

    private val _updateResult = MutableSharedFlow<UpdateResult>()
    val updateResult = _updateResult.asSharedFlow()

    init {
        subscribeOnNetworkStatus()
        logAnalytics()
        viewModelScope.launch(exceptionHandler) {
            if (currentUser.value.email.value.isNotEmpty()) {
                return@launch
            }
            val profile = getMyProfileUseCase.execute().toUserProfileModel()
            val state = EditProfileScreeState(
                avatarString = AvatarString(profile.userAvatar),
                firstName = FirstName(profile.userFirstName),
                lastName = LastName(profile.userLastName),
                phone = Phone(profile.userPhoneNumber),
                email = Email(profile.userEmail)
            )
            _currentUser.emit(state)
        }
    }

    private fun logAnalytics() {
        viewModelScope.launch(exceptionHandler) {
            hitMeUpFirebaseAnalytics
                .getAnalytics()
                .logEvent(
                    FirebaseAnalytics.Event.SCREEN_VIEW, bundleOf(
                        FirebaseAnalytics.Param.SCREEN_NAME to
                                Constants.ANALYTICS_EVENT_OPEN_SCREEN_EDIT_PROFILE,

                        FirebaseAnalytics.Param.ITEM_ID to
                                dbRepository.getUserProfile()?.user_id
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
                            editModelField(HasInternet(true))
                        }

                        else -> {
                            editModelField(HasInternet(false))
                        }
                    }
                }
        }
    }

    fun editModelField(params: StateParams) {
        viewModelScope.launch(exceptionHandler) {
            when (params) {
                is FirstNameError -> {
                    val newVal = _currentUser.value.copy(firstNameError = params)
                    _currentUser.emit(newVal)
                }

                is LastNameError -> {
                    val newVal = _currentUser.value.copy(lastNameError = params)
                    _currentUser.emit(newVal)
                }

                is PhoneError -> {
                    val newVal = _currentUser.value.copy(phoneError = params)
                    _currentUser.emit(newVal)
                }

                is HasInternet -> {
                    val newVal = _currentUser.value.copy(hasInternet = params)
                    _currentUser.emit(newVal)
                }

                is AvatarUri -> {
                    val newVal = _currentUser.value.copy(avatarUri = params)
                    _currentUser.emit(newVal)
                }

                is FirstName -> {
                    val newVal = _currentUser.value.copy(firstName = params)
                    _currentUser.emit(newVal)
                }

                is LastName -> {
                    val newVal = _currentUser.value.copy(lastName = params)
                    _currentUser.emit(newVal)
                }

                is Phone -> {
                    val newVal = _currentUser.value.copy(phone = params)
                    _currentUser.emit(newVal)
                }

                is AvatarString -> {
                    val newVal = _currentUser.value.copy(avatarString = params)
                    _currentUser.emit(newVal)
                }

                is Email -> {
                    val newVal = _currentUser.value.copy(email = params)
                    _currentUser.emit(newVal)
                }
            }
        }
    }

    fun updateProfileClicked() {
        viewModelScope.launch(exceptionHandler) {
            _updateResult.emit(UpdateResult.Loading(0))

            if (_currentUser.value.avatarUri.value.isNotEmpty()) {
                uploadImageUseCase.execute(Uri.parse(_currentUser.value.avatarUri.value))
                    .collect { uploadResult ->
                        when (uploadResult) {
                            is UploadMediaResult.Error -> _updateResult.emit(
                                UpdateResult.Error(uploadResult.message)
                            )

                            is UploadMediaResult.Loading -> {
                                val percent =
                                    ((uploadResult.transferred * 100) / uploadResult.total)
                                _updateResult.emit(UpdateResult.Loading(percent.toInt()))
                            }

                            is UploadMediaResult.Success -> {
                                val newVal =
                                    _currentUser.value.copy(avatarString = AvatarString(uploadResult.url))
                                val updateUser = updateMyProfileUseCase.execute(newVal)
                                _updateResult.emit(updateUser)
                            }
                        }
                    }
            } else {
                _updateResult.emit(updateMyProfileUseCase.execute(_currentUser.value))
            }
        }
    }

    fun updateNetworkStatus() {
        editModelField(HasInternet(connectivityManager.isOnline()))
    }

    companion object {
        private const val EDIT_PROFILE_KEY = "edit_profile_key"
    }
}

data class EditProfileScreeState(
    val email: Email = Email(),
    val hasInternet: HasInternet = HasInternet(),

    val firstName: FirstName = FirstName(),
    val firstNameError: FirstNameError = FirstNameError(),

    val lastName: LastName = LastName(),
    val lastNameError: LastNameError = LastNameError(),

    val phone: Phone = Phone(),
    val phoneError: PhoneError = PhoneError(),

    val avatarString: AvatarString = AvatarString(),
    val avatarUri: AvatarUri = AvatarUri(),
) : Serializable

sealed class StateParams : Serializable {
    data class HasInternet(val value: Boolean = true) : StateParams()
    data class AvatarString(val value: String = "") : StateParams()
    data class Email(val value: String = "") : StateParams()
    data class AvatarUri(val value: String = "") : StateParams()
    data class FirstName(val value: String = "") : StateParams()
    data class FirstNameError(val value: String = "") : StateParams()
    data class LastName(val value: String = "") : StateParams()
    data class LastNameError(val value: String = "") : StateParams()
    data class Phone(val value: String = "") : StateParams()
    data class PhoneError(val value: String = "") : StateParams()
}

sealed class UpdateResult : Serializable {
    data class Loading(val percentage: Int) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
    data class Success(val user: EditProfileScreeState) : UpdateResult()
}