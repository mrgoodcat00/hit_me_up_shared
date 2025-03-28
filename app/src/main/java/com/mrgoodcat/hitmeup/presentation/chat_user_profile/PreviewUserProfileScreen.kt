package com.mrgoodcat.hitmeup.presentation.chat_user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.UserModel
import com.mrgoodcat.hitmeup.presentation.ui.component.StatefulProgressComponent
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyBottomBarBackground
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Composable
fun PreviewUserProfileScreen(
    navController: NavHostController,
    userId: String,
    profilePreviewViewModel: PreviewUserProfileScreen = hiltViewModel(),
) {
    var currentUser by remember { mutableStateOf(UserModel()) }
    var isProgressActive by remember { mutableStateOf(false) }
    var latch by remember { mutableIntStateOf(1) }

    LaunchedEffect(key1 = Unit) {
        isProgressActive = true
        profilePreviewViewModel.getCurrentUser(userId)
        profilePreviewViewModel.screenState.collect { state ->
            when (state) {
                is ProfilePreviewScreenState.IsReady -> {
                    currentUser = state.profile
                    isProgressActive = false
                }

                ProfilePreviewScreenState.Loading -> {
                    isProgressActive = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 13.dp, end = 49.dp)
                    .background(Color.White)
            ) {
                IconButton(
                    onClick = {
                        if (latch == 0) return@IconButton
                        navController.popBackStack()
                        latch--
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_top_back_navigation),
                        tint = LightBlueBorder,
                        contentDescription = "",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.preview_profile_title),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = BlackTitle,
                    fontFamily = Roboto,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.5.sp
                )
            }
        },
        bottomBar = {

        },
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White)
            .systemBarsPadding()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight()
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentUser.user_avatar.isEmpty()) {
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
                            if (currentUser.user_first_name.length > 1) currentUser.user_first_name.substring(
                                0,
                                1
                            ) else ""
                        val lName =
                            if (currentUser.user_last_name.length > 1) currentUser.user_last_name.substring(
                                0,
                                1
                            ) else ""
                        Text(
                            text = "$fName$lName".toUpperCase(Locale.current),
                            textAlign = TextAlign.Center,
                            fontSize = 49.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                } else {

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentUser.user_avatar)
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
                        text = "${currentUser.user_first_name} ${currentUser.user_last_name}",
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        color = BlackTitle,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
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
                        currentUser.user_phone.ifEmpty { stringResource(id = R.string.no_phone_number) }

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
                        currentUser.user_email.ifEmpty { stringResource(id = R.string.no_email) }
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
            }
        }
    }

    if (isProgressActive) {
        StatefulProgressComponent()
    }


}