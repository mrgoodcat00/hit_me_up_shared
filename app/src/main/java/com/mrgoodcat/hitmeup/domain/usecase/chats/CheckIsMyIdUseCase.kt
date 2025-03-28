package com.mrgoodcat.hitmeup.domain.usecase.chats

import com.mrgoodcat.hitmeup.domain.repository.AuthorizationRepository
import javax.inject.Inject

open class CheckIsMyIdUseCase @Inject constructor(
    private val authRepo: AuthorizationRepository
) {
    fun execute(id: String): Boolean {
        return authRepo.itsMyId(id)
    }
}