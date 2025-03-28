package com.mrgoodcat.hitmeup.domain.model

class Constants {
    companion object {
        const val NAV_DESTINATION_SPLASH_SCREEN = "nav_destination_splash_screen"
        const val NAV_DESTINATION_LOGIN_SCREEN = "nav_destination_login_screen"
        const val NAV_DESTINATION_REGISTRATION_SCREEN = "nav_destination_registration_screen"
        const val NAV_DESTINATION_CHATS_SCREEN = "nav_destination_chats_screen"
        const val NAV_DESTINATION_MESSAGES_SCREEN = "nav_destination_messages_screen"
        const val NAV_DESTINATION_CONTACTS_SCREEN = "nav_destination_contacts_screen"
        const val NAV_DESTINATION_PROFILE_SCREEN = "nav_destination_profile_screen"
        const val NAV_DESTINATION_PROFILE_PREVIEW_SCREEN = "nav_destination_profile_preview_screen"
        const val NAV_DESTINATION_EDIT_PROFILE_SCREEN = "nav_destination_edit_profile_screen"
        const val NAV_DESTINATION_SEARCH_USERS_SCREEN = "nav_destination_search_users_screen"
    }

    sealed class HitMeUpScreen(val route: String) {
        data object SplashScreen : HitMeUpScreen(NAV_DESTINATION_SPLASH_SCREEN)
        data object LoginScreen : HitMeUpScreen(NAV_DESTINATION_LOGIN_SCREEN)
        data object RegistrationScreen : HitMeUpScreen(NAV_DESTINATION_REGISTRATION_SCREEN)
        data object ChatsScreen : HitMeUpScreen(NAV_DESTINATION_CHATS_SCREEN)
        data object MessagesScreen : HitMeUpScreen(NAV_DESTINATION_MESSAGES_SCREEN)
        data object ContactsScreen : HitMeUpScreen(NAV_DESTINATION_CONTACTS_SCREEN)
        data object SearchContactsScreen : HitMeUpScreen(NAV_DESTINATION_SEARCH_USERS_SCREEN)
        data object ProfileScreen : HitMeUpScreen(NAV_DESTINATION_PROFILE_SCREEN)
        data object ProfilePreviewScreen : HitMeUpScreen(NAV_DESTINATION_PROFILE_PREVIEW_SCREEN)
        data object EditProfileScreen : HitMeUpScreen(NAV_DESTINATION_EDIT_PROFILE_SCREEN)
    }
}