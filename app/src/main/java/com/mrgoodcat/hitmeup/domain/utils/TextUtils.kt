package com.mrgoodcat.hitmeup.domain.utils

import android.content.Context
import com.mrgoodcat.hitmeup.R
import javax.inject.Inject

class TextUtils @Inject constructor(private val context: Context) {

    fun validateEmail(email: String): ValidateResult {
        if (email.isEmpty()) {
            return ValidateResult.IsNotOk(
                context.getString(R.string.email_is_empty)
            )
        }
        return ValidateResult.IsOk()
    }

    fun validatePassword(password: String): ValidateResult {
        if (password.isEmpty()) {
            return ValidateResult.IsNotOk(
                context.getString(R.string.password_is_empty)
            )
        }
        return ValidateResult.IsOk()
    }

    fun validateRepeatedPassword(password: String, passwordRepeated: String): ValidateResult {
        if (password != passwordRepeated) {
            return ValidateResult.IsNotOk(
                context.getString(R.string.password_repeate_error)
            )
        }
        return ValidateResult.IsOk()
    }

    sealed class ValidateResult {
        data class IsOk(val returnParamVal: Any = Any()) :
            ValidateResult()

        data class IsNotOk(val errorCode: String = "") :
            ValidateResult()
    }
}