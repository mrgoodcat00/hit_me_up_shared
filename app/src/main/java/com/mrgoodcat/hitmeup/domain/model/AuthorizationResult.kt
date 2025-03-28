package com.mrgoodcat.hitmeup.domain.model

import com.google.firebase.auth.FirebaseUser

sealed class AuthorizationResult {
    data class ResultSuccessful(val user: FirebaseUser?) : AuthorizationResult()
    data class ResultFailed(val message: String) : AuthorizationResult()
    data class ResultFalse(val message: String = "") : AuthorizationResult()
    data object ResultCanceled : AuthorizationResult()
    data object IsLoading : AuthorizationResult()
}