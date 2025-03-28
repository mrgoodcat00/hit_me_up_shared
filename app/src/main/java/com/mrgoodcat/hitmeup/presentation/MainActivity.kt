package com.mrgoodcat.hitmeup.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.NOTIFICATION_CHANNEL_DEFAULT_ID
import com.mrgoodcat.hitmeup.data.push_notification.Constants.Companion.NOTIFICATION_CHANNEL_DEFAULT_NAME
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.ChatsScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.ContactsScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.EditProfileScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.LoginScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.MessagesScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.ProfilePreviewScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.ProfileScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.RegistrationScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.SearchContactsScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.SplashScreen
import com.mrgoodcat.hitmeup.presentation.chat_user_profile.PreviewUserProfileScreen
import com.mrgoodcat.hitmeup.presentation.chats.ChatsScreen
import com.mrgoodcat.hitmeup.presentation.contacts.ContactsScreen
import com.mrgoodcat.hitmeup.presentation.edit_profile.EditProfileScreen
import com.mrgoodcat.hitmeup.presentation.home.AuthState
import com.mrgoodcat.hitmeup.presentation.home.BaseViewModel
import com.mrgoodcat.hitmeup.presentation.login.LoginScreen
import com.mrgoodcat.hitmeup.presentation.messages.MessagesScreen
import com.mrgoodcat.hitmeup.presentation.profile.ProfileScreen
import com.mrgoodcat.hitmeup.presentation.registration.RegistrationScreen
import com.mrgoodcat.hitmeup.presentation.search_contacts.SearchContactsScreen
import com.mrgoodcat.hitmeup.presentation.splash.SplashScreen
import com.mrgoodcat.hitmeup.presentation.ui.theme.HitMeUpTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

@SuppressLint("RestrictedApi")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val baseViewModel: BaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            installSplashScreen().apply {
                setKeepOnScreenCondition {
                    baseViewModel.splashAnimationDone.value
                }
            }
        }

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        setContent {
            HitMeUpTheme(darkTheme = false) {

                val context = LocalContext.current

                val navController = rememberNavController()

                LaunchedEffect(key1 = Unit) {
                    baseViewModel.authorizationFlag.collect { authState ->
                        when (authState) {
                            is AuthState.Error -> {}

                            is AuthState.LoggedIn -> {
                                if (authState.isLoggedIn) {
                                    navController.navigate(ChatsScreen.route) {
                                        popUpTo(0)
                                    }
                                } else {
                                    navController.navigate(LoginScreen.route) {
                                        popUpTo(0)
                                    }
                                }
                            }
                        }
                    }
                }

                var timeLastBackNavigate by remember { mutableLongStateOf(0L) }
                BackHandler(true) {
                    if (navController.currentBackStack.value.size == 2) {
                        val timePassed = System.currentTimeMillis() - timeLastBackNavigate
                        if (timeLastBackNavigate == 0L || timePassed > 2000L) {
                            timeLastBackNavigate = System.currentTimeMillis()
                            Toast.makeText(
                                context,
                                getString(R.string.press_again_to_close_the_app),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            finish()
                        }
                    } else {
                        navController.popBackStack()
                    }
                }

                LaunchedEffect(key1 = Unit) {
                    navController.currentBackStackEntryFlow.collect { navBackStackEntry ->
                        baseViewModel.updateCurrentScreenName(navBackStackEntry)
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = SplashScreen.route
                ) {
                    composable(route = SplashScreen.route) {
                        SplashScreen(baseViewModel)
                    }

                    composable(route = LoginScreen.route) {
                        LoginScreen { clearBackStack, route ->
                            navController.navigate(route) {
                                if (clearBackStack) {
                                    popUpTo(0)
                                }
                            }
                        }
                    }

                    composable(route = RegistrationScreen.route) {
                        RegistrationScreen(baseViewModel = baseViewModel) { route ->
                            navController.navigate(route) {
                                popUpTo(0)
                            }
                        }
                    }

                    composable(route = ChatsScreen.route) {
                        ChatsScreen(navController,
                            chatClick = { clickedChat ->
                                navController.navigate(
                                    route = "${MessagesScreen.route}/${clickedChat.id}"
                                )
                            }, previewProfile = { userModel ->
                                navController.navigate(route = "${ProfilePreviewScreen.route}/${userModel.user_id}")
                            })
                    }

                    composable(
                        route = "${MessagesScreen.route}/{$ARGUMENT_NAV_CHAT_ID}",
                        arguments = listOf(navArgument(ARGUMENT_NAV_CHAT_ID) {
                            type = NavType.StringType
                        })
                    ) { navBackStackEntry ->
                        val chatId = navBackStackEntry.arguments?.getString(ARGUMENT_NAV_CHAT_ID)
                            ?: return@composable
                        MessagesScreen(
                            navController,
                            chatId = chatId,
                            onPreviewClicked = { userModel ->
                                navController.navigate(route = "${ProfilePreviewScreen.route}/${userModel.user_id}")
                            }
                        )
                    }

                    composable(route = ContactsScreen.route) {
                        ContactsScreen(navController,
                            previewProfile = { friendModel ->
                                navController.navigate(route = "${ProfilePreviewScreen.route}/${friendModel.useId}")
                            }, createChatWith = { createdChat ->
                                navController.navigate(route = "${MessagesScreen.route}/${createdChat.id}")
                            })
                    }

                    composable(route = SearchContactsScreen.route) {
                        SearchContactsScreen(
                            navController = navController,
                            previewProfile = { friend ->
                                navController.navigate(route = "${ProfilePreviewScreen.route}/${friend.useId}")
                            },
                            createChatWith = { createdChat ->
                                navController.navigate(route = "${MessagesScreen.route}/${createdChat.id}")
                            })
                    }

                    composable(route = ProfileScreen.route) {
                        ProfileScreen(navController = navController) {
                            navController.navigate(it)
                        }
                    }

                    composable(
                        route = "${ProfilePreviewScreen.route}/{$ARGUMENT_NAV_USER_ID}",
                        arguments = listOf(navArgument(ARGUMENT_NAV_USER_ID) {
                            type = NavType.StringType
                        })
                    ) { navBackstackEntry ->
                        val userId = navBackstackEntry.arguments?.getString(ARGUMENT_NAV_USER_ID)
                            ?: return@composable
                        PreviewUserProfileScreen(navController, userId)
                    }

                    composable(route = EditProfileScreen.route) {
                        EditProfileScreen(navController = navController)
                    }
                }
            }
        }
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_DEFAULT_ID,
            NOTIFICATION_CHANNEL_DEFAULT_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ARGUMENT_NAV_CHAT_ID = "chat_id"
        const val ARGUMENT_NAV_USER_ID = "user_id"
    }
}

@Composable
inline fun <reified T> Flow<T>.collectWithLifecycle(
    key: Any = Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit,
) {
    val lifecycleAwareFlow = remember(this, lifecycleOwner) {
        flowWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            minActiveState = minActiveState
        )
    }

    LaunchedEffect(key) {
        lifecycleAwareFlow.collect { action(it) }
    }
}

internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException(context.getString(R.string.no_context_exception))
}

fun <T> SavedStateHandle.getStateFlow(
    scope: CoroutineScope,
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    val liveData = getLiveData(key, initialValue)
    val stateFlow = MutableStateFlow(initialValue)

    val observer = Observer<T> { value ->
        if (stateFlow.value != value) {
            stateFlow.value = value
        }
    }

    liveData.observeForever(observer)

    stateFlow.onCompletion {
        withContext(Dispatchers.Main.immediate) {
            liveData.removeObserver(observer)
        }
    }.onEach { value ->
        withContext(Dispatchers.Main.immediate) {
            if (liveData.value != value) {
                liveData.value = value
            }
        }
    }.launchIn(scope)

    return stateFlow
}