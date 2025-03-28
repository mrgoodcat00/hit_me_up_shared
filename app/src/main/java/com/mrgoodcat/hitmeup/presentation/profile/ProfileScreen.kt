package com.mrgoodcat.hitmeup.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.EditProfileScreen
import com.mrgoodcat.hitmeup.presentation.chats.components.ConfirmationDialog
import com.mrgoodcat.hitmeup.presentation.chats.components.ConfirmationDialogParams
import com.mrgoodcat.hitmeup.presentation.profile.StateParams.IsDeleteDialog
import com.mrgoodcat.hitmeup.presentation.profile.StateParams.IsLoading
import com.mrgoodcat.hitmeup.presentation.ui.component.BottomNavigationBar
import com.mrgoodcat.hitmeup.presentation.ui.component.StatefulProgressComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.TopBarComponent
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyBottomBarBackground
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    navigate: (String) -> Unit
) {

    val screenState by profileViewModel.profileScreenState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(key1 = Unit) {
        profileViewModel.updateNetworkStatus()
        profileViewModel.getCurrentUser()
    }

    Scaffold(
        topBar = {
            TopBarComponent(
                title = stringResource(id = R.string.profile_screen_title),
                useDivider = false,
                hasInternet = screenState.hasInternet.value
            ) {

                IconButton(onClick = {
                    if (!screenState.hasInternet.value) {
                        return@IconButton
                    }
                    profileViewModel.editScreenState(IsDeleteDialog(true))
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person_remove_24),
                        contentDescription = null,
                        modifier = Modifier
                            .requiredSize(32.dp),
                        colorResource(id = R.color.light_blue_top_icon)
                    )
                }

                IconButton(onClick = {
                    if (!screenState.hasInternet.value) {
                        return@IconButton
                    }
                    navigate(EditProfileScreen.route)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit_profile),
                        contentDescription = null,
                        modifier = Modifier
                            .requiredSize(24.dp),
                        colorResource(id = R.color.light_blue_top_icon)
                    )
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = navBackStackEntry?.destination?.route ?: "",
                onItemClick = { navController.navigate(it.route) }
            )
        },
        modifier = Modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight()
                .fillMaxWidth()
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (screenState.userModel.value.userAvatar.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(98.dp)
                            .height(98.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(Color.Green, Color.Yellow),
                                    Offset.Infinite
                                ), shape = CircleShape, 0.6F
                            )
                    ) {
                        val fName =
                            if (screenState.userModel.value.userFirstName.length > 1) screenState.userModel.value.userFirstName.substring(
                                0,
                                1
                            ) else ""
                        val lName =
                            if (screenState.userModel.value.userLastName.length > 1) screenState.userModel.value.userLastName.substring(
                                0,
                                1
                            ) else ""
                        Text(
                            text = "$fName$lName".toUpperCase(Locale.current),
                            textAlign = TextAlign.Center,
                            fontSize = 29.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(screenState.userModel.value.userAvatar)
                            .crossfade(true)
                            .error(R.drawable.fui_ic_anonymous_white_24dp)
                            .build(),
                        placeholder = painterResource(id = R.drawable.fui_ic_anonymous_white_24dp),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(98.dp)
                            .height(98.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(Color.LightGray, Color.DarkGray),
                                    Offset.Infinite
                                ), shape = CircleShape, 0.5F
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                SelectionContainer {
                    Text(
                        text = "${screenState.userModel.value.userFirstName} ${screenState.userModel.value.userLastName}",
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        color = BlackTitle,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = Roboto,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(
                            GreyBottomBarBackground, RoundedCornerShape(
                                topStart = 5.dp,
                                topEnd = 5.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .padding(start = 10.dp, top = 11.dp, end = 10.dp, bottom = 7.dp)

                ) {
                    Text(
                        text = stringResource(id = R.string.phone_field_name),
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Start,
                        color = BlackTitle,
                        fontFamily = Roboto
                    )

                    val phone =
                        screenState.userModel.value.userPhoneNumber.ifEmpty { stringResource(id = R.string.no_phone_number) }
                    SelectionContainer {
                        Text(
                            text = phone,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Start,
                            color = LightBlueBorder,
                            fontFamily = Roboto,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(
                            GreyBottomBarBackground, RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 5.dp,
                                bottomEnd = 5.dp
                            )
                        )
                        .padding(start = 10.dp, top = 11.dp, end = 10.dp, bottom = 7.dp)

                ) {
                    Text(
                        text = stringResource(id = R.string.email_field_name),
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Start,
                        color = BlackTitle,
                        fontFamily = Roboto
                    )

                    val phone =
                        screenState.userModel.value.userEmail.ifEmpty { stringResource(id = R.string.no_email) }
                    SelectionContainer {
                        Text(
                            text = phone,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Start,
                            color = LightBlueBorder,
                            fontFamily = Roboto,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (!screenState.hasInternet.value) {
                            return@Button
                        }
                        profileViewModel.editScreenState(IsLoading(true))
                        profileViewModel.logoutUser()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = LightBlueBorder)
                ) {
                    Text(
                        text = stringResource(id = R.string.logout_button_title),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Roboto,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    if (screenState.loading.value) {
        StatefulProgressComponent()
    }

    if (screenState.isDeleteDialog.value) {
        ConfirmationDialog(
            ConfirmationDialogParams(
                title = stringResource(id = R.string.delete_account_warning_title),
                text = stringResource(id = R.string.delete_account_warning_text),
                confirmButtonText = stringResource(id = R.string.delete_account_warning_delete_button)
            ),
            cancel = {
                profileViewModel.editScreenState(IsDeleteDialog())
            },
            confirm = {
                profileViewModel.editScreenState(IsLoading(true))
                profileViewModel.deleteUser()
            }
        )
    }
}