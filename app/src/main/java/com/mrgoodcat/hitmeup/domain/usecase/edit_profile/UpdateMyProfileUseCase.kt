package com.mrgoodcat.hitmeup.domain.usecase.edit_profile

import android.content.Context
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import com.mrgoodcat.hitmeup.presentation.edit_profile.EditProfileScreeState
import com.mrgoodcat.hitmeup.presentation.edit_profile.UpdateResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

open class UpdateMyProfileUseCase @Inject constructor(
    private val dbRepository: DbRepository,
    private val firebaseUsersApi: FirebaseUsersApi,
    @ApplicationContext private val context: Context,
) {
    suspend fun execute(user: EditProfileScreeState): UpdateResult {
        if (user.firstName.value.isEmpty()) {
            return UpdateResult.Error(context.getString(R.string.first_name_field_error))
        }

        var currentUserProfile = dbRepository.getUserProfile() ?: UserProfileLocalModel()
        var currentUser = firebaseUsersApi.getUserById(currentUserProfile.user_id)

        currentUser = currentUser.copy(
            user_avatar = user.avatarString.value,
            user_phone = user.phone.value,
            user_first_name = user.firstName.value,
            user_last_name = user.lastName.value
        )

        val updatedUser = firebaseUsersApi.updateUser(currentUser)

        currentUserProfile = currentUserProfile.copy(
            user_first_name = updatedUser.user_first_name,
            user_last_name = updatedUser.user_last_name,
            user_phone = updatedUser.user_phone,
            user_avatar = updatedUser.user_avatar
        )

        dbRepository.insertUserProfile(currentUserProfile)
        return UpdateResult.Success(user)
    }
}