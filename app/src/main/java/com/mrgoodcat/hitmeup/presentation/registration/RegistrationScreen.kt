package com.mrgoodcat.hitmeup.presentation.registration

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.LoginScreen
import com.mrgoodcat.hitmeup.presentation.home.BaseViewModel
import com.mrgoodcat.hitmeup.presentation.registration.CreateUserResult.Error
import com.mrgoodcat.hitmeup.presentation.registration.StateParams.ExistUserDialogState
import com.mrgoodcat.hitmeup.presentation.registration.components.ConfirmationRegistrationDialogComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.InputFieldComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.NoInternetMessageComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.StatefulProgressComponent
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightGreyBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto
import timber.log.Timber
import java.lang.ref.WeakReference

@Preview
@Composable
fun RegistrationScreen(
    registrationViewModel: RegistrationViewModel = hiltViewModel(),
    baseViewModel: BaseViewModel = hiltViewModel(),
    navigate: (String) -> Unit = {}
) {

    val registerScreenState by registrationViewModel.registerScreenState.collectAsState()
    val registerResult by registrationViewModel.registerResult.collectAsState()

    val registryOwner =
        WeakReference<ActivityResultRegistryOwner>(LocalActivityResultRegistryOwner.current)
    val localContext = LocalContext.current
    val localActivity = WeakReference<Activity>(LocalActivity.current)

    LaunchedEffect(key1 = Unit) {
        baseViewModel.removeAuthListener()
    }

    LaunchedEffect(key1 = Unit) {
        registrationViewModel.updateNetworkStatus()
    }

    LaunchedEffect(key1 = registerResult) {
        when (registerResult) {
            is Error -> {
                registrationViewModel.releaseUserAuthorization()
                Toast.makeText(
                    localContext,
                    "Error with create user",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is CreateUserResult.Success -> {
                Timber.d("RegistrationScreen: Success")
                baseViewModel.removeAuthListener()
                baseViewModel.addAuthListener()
            }

            null -> {}
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 13.dp, vertical = 0.dp)
                    .background(Color.White)
            ) {
                IconButton(
                    onClick = {
                        if (!registerScreenState.hasInternet.value) {
                            return@IconButton
                        }
                        registrationViewModel.releaseUserAuthorization()
                        baseViewModel.removeAuthListener()
                        baseViewModel.addAuthListener()
                        navigate(LoginScreen.route)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_top_back_navigation),
                        tint = Color.Unspecified,
                        contentDescription = "",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.register_screen_title),
                    textAlign = TextAlign.Start,
                    color = BlackTitle,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.5.sp
                )
            }
            NoInternetMessageComponent(hasInternet = registerScreenState.hasInternet.value)
        }
    ) { padding ->

        if (registerScreenState.existUserDialogState.value) {
            ConfirmationRegistrationDialogComponent(
                userModel = registerScreenState.existedUser.value,
                cancel = {
                    registrationViewModel.editScreenState(ExistUserDialogState(false))
                    registrationViewModel.releaseUserAuthorization()
                },
                authorizeWithCurrentUser = { userModel ->
                    registrationViewModel.editScreenState(ExistUserDialogState(false))
                    registrationViewModel.updateFcmToken(userModel.user_id)
                    baseViewModel.removeAuthListener()
                    baseViewModel.addAuthListener()
                },
                createAccount = { userModel ->
                    registrationViewModel.editScreenState(ExistUserDialogState(false))
                    registrationViewModel.releaseUserAuthorization()
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(padding)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = 5.dp, top = 0.dp, start = 24.dp, end = 24.dp)
                    .background(Color.White)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.register_screen_subtitle),
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Start,
                    color = GrayTitle,
                    fontFamily = Roboto,
                    letterSpacing = 0.sp
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Email input
                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.email_field_name),
                    fieldText = registerScreenState.loginString.value,
                    fieldErrorText = registerScreenState.loginErrorString.value,
                    isFieldError = registerScreenState.loginErrorState.value,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    isRequired = true,
                    placeHolderText = stringResource(id = R.string.email_field_placeholder)
                ) { registrationViewModel.editScreenState(StateParams.LoginString(it)) }

                // Password input
                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.password_field_name),
                    fieldText = registerScreenState.passwordString.value,
                    fieldErrorText = registerScreenState.passwordStringError.value,
                    isFieldError = registerScreenState.passwordStateError.value,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    isRequired = true,
                    placeHolderText = stringResource(id = R.string.password_field_placeholder)
                ) { registrationViewModel.editScreenState(StateParams.PasswordString(it)) }

                // Password input
                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.password_repeate_field_name),
                    fieldText = registerScreenState.passwordStringRepeat.value,
                    fieldErrorText = registerScreenState.passwordStringErrorRepeat.value,
                    isFieldError = registerScreenState.passwordStateErrorRepeat.value,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    isRequired = true,
                    placeHolderText = stringResource(id = R.string.password_repeate_placeholder),
                ) { registrationViewModel.editScreenState(StateParams.PasswordStringRepeat(it)) }

                Spacer(modifier = Modifier.height(16.dp))

                // SignIn with Email Button
                Button(
                    onClick = {
                        if (!registerScreenState.hasInternet.value) {
                            return@Button
                        }
                        registrationViewModel.validateAndRegisterWithEmail()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = LightBlueBorder)
                ) {
                    Text(
                        text = stringResource(id = R.string.register_button_text),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.register_subbuton_text),
                    textAlign = TextAlign.Center,
                    color = GrayTitle,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 16.sp,
                    fontSize = 13.sp,
                    fontFamily = Roboto,
                    letterSpacing = 0.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SignIn Google
                OutlinedButton(
                    onClick = {
                        if (!registerScreenState.hasInternet.value) {
                            return@OutlinedButton
                        }
                        registrationViewModel.registerUserWithGoogle(localActivity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, LightGreyBorder),
                ) {
                    Icon(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        painter = painterResource(id = R.drawable.ic_google_vector),
                        tint = Color.Unspecified,
                        contentDescription = stringResource(id = R.string.register_with_google_button),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.register_with_google_button),
                        color = BlackTitle,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        fontFamily = Roboto
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SignIn FB
                OutlinedButton(
                    onClick = {
                        if (!registerScreenState.hasInternet.value) {
                            return@OutlinedButton
                        }

                        registrationViewModel.registerUserWithFacebook(registryOwner)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, LightGreyBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                    )
                ) {
                    Icon(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        painter = painterResource(id = R.drawable.ic_facebook_vector),
                        tint = Color.Unspecified,
                        contentDescription = stringResource(id = R.string.register_with_fb_button),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.register_with_fb_button),
                        color = BlackTitle,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        fontFamily = Roboto
                    )
                }
            }
        }
    }

    if (registerScreenState.isLoading.value) {
        Timber.d("registerScreenState.isLoading ${registerScreenState.isLoading.value}")
        StatefulProgressComponent()
    }

}
