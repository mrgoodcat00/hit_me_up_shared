package com.mrgoodcat.hitmeup.presentation.chat_user_profile

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.mrgoodcat.hitmeup.data.analitycs.Constants
import com.mrgoodcat.hitmeup.domain.analitycs.HitMeUpFirebaseAnalytics
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.domain.model.extensions.toUserModel
import com.mrgoodcat.hitmeup.domain.usecase.GetUserByIdFromApiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PreviewUserProfileScreen @Inject constructor(
    private val getUserByIdFromApiUseCase: GetUserByIdFromApiUseCase,
    private val hitMeUpFirebaseAnalytics: HitMeUpFirebaseAnalytics,
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("CoroutineExceptionHandler got $exception")
    }

    private var _screenState = MutableSharedFlow<ProfilePreviewScreenState>()
    val screenState: SharedFlow<ProfilePreviewScreenState> = _screenState.asSharedFlow()

    fun getCurrentUser(userId: String) {
        viewModelScope.launch(exceptionHandler) {

            val bundle = Bundle()
            bundle.putString(
                FirebaseAnalytics.Param.SCREEN_NAME,
                Constants.ANALYTICS_EVENT_OPEN_SCREEN_USER_PREVIEW
            )
            bundle.putString(
                FirebaseAnalytics.Param.ITEM_LIST_ID,
                userId
            )
            hitMeUpFirebaseAnalytics
                .getAnalytics()
                .logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)

            _screenState.emit(ProfilePreviewScreenState.Loading)
            _screenState.emit(
                ProfilePreviewScreenState.IsReady(
                    getUserByIdFromApiUseCase.execute(
                        userId
                    ).toUserModel()
                )
            )
        }
    }
}

sealed class ProfilePreviewScreenState {
    data object Loading : ProfilePreviewScreenState()
    data class IsReady(val profile: UserModel) : ProfilePreviewScreenState()
}