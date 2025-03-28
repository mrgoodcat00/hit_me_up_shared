package com.mrgoodcat.hitmeup.domain.model

import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_CHATS_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_CONTACTS_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_EDIT_PROFILE_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_LOGIN_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_MESSAGES_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_PROFILE_PREVIEW_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_PROFILE_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_REGISTRATION_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_SEARCH_USERS_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.Companion.NAV_DESTINATION_SPLASH_SCREEN
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen

data class AppSettingsModel(
    val id: Int = 0,
    val currentScreen: String = "",
    val isUserVerified: Boolean = true,
    val currentOpenedChatId: String = "",
) {
    fun getCurrentScreenName(): HitMeUpScreen {
        return when (currentScreen) {
            NAV_DESTINATION_SPLASH_SCREEN -> {
                HitMeUpScreen.SplashScreen
            }

            NAV_DESTINATION_LOGIN_SCREEN -> {
                HitMeUpScreen.LoginScreen
            }

            NAV_DESTINATION_REGISTRATION_SCREEN -> {
                HitMeUpScreen.RegistrationScreen
            }

            NAV_DESTINATION_CHATS_SCREEN -> {
                HitMeUpScreen.ChatsScreen
            }

            NAV_DESTINATION_MESSAGES_SCREEN -> {
                HitMeUpScreen.MessagesScreen
            }

            NAV_DESTINATION_CONTACTS_SCREEN -> {
                HitMeUpScreen.ContactsScreen
            }

            NAV_DESTINATION_PROFILE_SCREEN -> {
                HitMeUpScreen.ProfileScreen
            }

            NAV_DESTINATION_PROFILE_PREVIEW_SCREEN -> {
                HitMeUpScreen.ProfilePreviewScreen
            }

            NAV_DESTINATION_EDIT_PROFILE_SCREEN -> {
                HitMeUpScreen.EditProfileScreen
            }

            NAV_DESTINATION_SEARCH_USERS_SCREEN -> {
                HitMeUpScreen.SearchContactsScreen
            }

            else -> {
                HitMeUpScreen.SplashScreen
            }
        }
    }
}