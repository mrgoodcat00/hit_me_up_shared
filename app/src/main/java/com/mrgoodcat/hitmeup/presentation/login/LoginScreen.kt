package com.mrgoodcat.hitmeup.presentation.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_ERROR_INVALID_EMAIL
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_ERROR_USER_NOT_FOUND
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.FIREBASE_ERROR_WRONG_PASSWORD
import com.mrgoodcat.hitmeup.domain.errors_map.Constants.Companion.GLOBAL_ERROR_USER_DOESNT_EXIST_IN_DB
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult
import com.mrgoodcat.hitmeup.domain.model.AuthorizationResult.ResultFailed
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.RegistrationScreen
import com.mrgoodcat.hitmeup.presentation.login.StateParams.LoginString
import com.mrgoodcat.hitmeup.presentation.login.StateParams.PasswordString
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
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navigate: (Boolean, String) -> Unit = { b: Boolean, s: String -> },
) {

    val loginState by loginViewModel.loginState.collectAsState()
    val registryOwner =
        WeakReference<ActivityResultRegistryOwner>(LocalActivityResultRegistryOwner.current)
    val localContext = LocalContext.current
    val localActivity = WeakReference<Activity>(LocalActivity.current)

    var isProgressActive by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        loginViewModel.updateNetworkStatus()
        loginViewModel.authorizationResult.collect {
            when (it) {
                AuthorizationResult.ResultCanceled -> {
                    isProgressActive = false
                    Timber.d("LoginScreen: LaunchedEffect: ResultCanceled")
                }

                is ResultFailed -> {
                    isProgressActive = false

                    Timber.d("LoginScreen: LaunchedEffect: ResultFailed ${it.message}")

                    val message = when (it.message) {
                        GLOBAL_ERROR_USER_DOESNT_EXIST_IN_DB -> {
                            localContext.getString(R.string.login_user_error_doesnt_exist)
                        }

                        FIREBASE_ERROR_INVALID_EMAIL -> {
                            localContext.getString(R.string.email_wrong_format_error)
                        }

                        FIREBASE_ERROR_USER_NOT_FOUND -> {
                            localContext.getString(R.string.login_user_error_doesnt_exist)
                        }

                        FIREBASE_ERROR_WRONG_PASSWORD -> {
                            localContext.getString(R.string.login_user_error_wrong_password)
                        }

                        else -> {
                            localContext.getString(R.string.load_data_error)
                        }
                    }

                    Toast.makeText(localContext, message, Toast.LENGTH_SHORT).show()
                }

                is AuthorizationResult.ResultFalse -> {
                    isProgressActive = false
                    Timber.d("LoginScreen: LaunchedEffect: ResultFalse ${it.message}")
                    Toast.makeText(
                        localContext,
                        "${it.message} go to Registration",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is AuthorizationResult.ResultSuccessful -> {
                    isProgressActive = false
                    Timber.d("LoginScreen: LaunchedEffect: success ${it.user?.uid}")
                }

                AuthorizationResult.IsLoading -> {
                    isProgressActive = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .systemBarsPadding(),
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 5.dp)
                    .background(Color.White)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.welcome_title),
                    textAlign = TextAlign.Start,
                    color = BlackTitle,
                    fontFamily = Roboto,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 19.sp,
                    lineHeight = 24.sp
                )
            }
            NoInternetMessageComponent(hasInternet = loginState.hasInternet.value)
        }
    ) { padding ->
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
                    .padding(bottom = 5.dp, start = 24.dp, end = 24.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.welcome_sub_title),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Start,
                    color = GrayTitle,
                    fontFamily = Roboto
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Email input
                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.email_field_name),
                    fieldText = loginState.loginString.value,
                    fieldErrorText = loginState.loginStringError.value,
                    isFieldError = loginState.loginStateError.value,
                    isRequired = true,
                    placeHolderText = stringResource(id = R.string.email_field_placeholder),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onValueChange = {
                        loginViewModel.editScreenState(LoginString(it))
                    }
                )

                // Password input
                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.password_field_name),
                    fieldText = loginState.passwordString.value,
                    fieldErrorText = loginState.passwordStringError.value,
                    isFieldError = loginState.passwordStateError.value,
                    isRequired = true,
                    placeHolderText = stringResource(id = R.string.password_field_placeholder),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    onValueChange = {
                        loginViewModel.editScreenState(PasswordString(it))
                    }
                )

                Spacer(modifier = Modifier.height(5.dp))

                // SignIn with Email Button
                Button(
                    onClick = {
                        if (!loginState.hasInternet.value) {
                            return@Button
                        }
                        loginViewModel.authorizeWithEmail()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = LightBlueBorder)
                ) {
                    Text(
                        text = stringResource(id = R.string.login_button_login_text),
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
                    text = stringResource(id = R.string.login_button_sub_login_text),
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
                        if (!loginState.hasInternet.value) {
                            return@OutlinedButton
                        }
                        loginViewModel.authorizeWithGoogle(localActivity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Unspecified),
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, LightGreyBorder),
                ) {
                    Icon(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        painter = painterResource(id = R.drawable.ic_google_vector),
                        tint = Color.Unspecified,
                        contentDescription = "",
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.signin_button_with_google),
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
                        if (!loginState.hasInternet.value) {
                            return@OutlinedButton
                        }
                        loginViewModel.authorizeWithFacebook(registryOwner)
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
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.signin_button_with_fb),
                        color = BlackTitle,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        fontFamily = Roboto
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.signin_buttons_sub_text),
                            Modifier.padding(2.dp),
                            color = GrayTitle,
                            fontSize = 13.sp,
                            lineHeight = 24.sp,
                            fontFamily = Roboto,
                            letterSpacing = 0.sp
                        )
                        Text(
                            text = stringResource(id = R.string.signup_button_text),
                            color = LightBlueBorder,
                            modifier = Modifier
                                .clickable {
                                    if (!loginState.hasInternet.value) {
                                        return@clickable
                                    }
                                    navigate(true, RegistrationScreen.route)
                                }
                                .padding(2.dp)
                                .drawBehind {
                                    val strokeWidthPx = 1.dp.toPx()
                                    val verticalOffset = size.height - 2.sp.toPx()
                                    drawLine(
                                        color = LightBlueBorder,
                                        strokeWidth = strokeWidthPx,
                                        start = Offset(0f, verticalOffset),
                                        end = Offset(size.width, verticalOffset)
                                    )
                                },
                            fontSize = 13.sp,
                            lineHeight = 24.sp,
                            fontFamily = Roboto,
                            letterSpacing = 0.sp
                        )
                    }
                }

            }
        }
    }

    if (isProgressActive) {
        StatefulProgressComponent()
    }
}