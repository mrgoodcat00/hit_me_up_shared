package com.mrgoodcat.hitmeup.domain.usecase.edit_profile

import com.mrgoodcat.hitmeup.data.model.UserProfileLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import javax.inject.Inject

open class GetMyProfileUseCase @Inject constructor(
    private val dbRepository: DbRepository
) {
    suspend fun execute(): UserProfileLocalModel =
        dbRepository.getUserProfile() ?: UserProfileLocalModel()
}