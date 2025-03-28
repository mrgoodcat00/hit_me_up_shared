package com.mrgoodcat.hitmeup.domain.errors_map

class Constants {
    companion object {
        const val GLOBAL_ERROR_USER_DOESNT_EXIST_IN_DB = "global_error_user_doesnt_exist_in_db"
        const val GLOBAL_ERROR_UNKNOWN_ERROR = "global_error_unknown_error"
        const val GLOBAL_ERROR_ACTION_CANCELLED = "global_error_action_cancelled"

        const val FIREBASE_ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
        const val FIREBASE_ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
        const val FIREBASE_ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD"
        const val FIREBASE_UNKNOWN_ERROR = "ERROR_UNKNOWN"
        const val FIREBASE_ERROR_TOO_MUCH_ATTEMPTS = "ERROR_TOO_MUCH_ATTEMPTS"
        const val FIREBASE_ERROR_EMAIL_IS_NOT_VERIFIED = "ERROR_EMAIL_IS_NOT_VERIFIED"
    }
}