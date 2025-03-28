package com.mrgoodcat.hitmeup.presentation.edit_profile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.presentation.edit_profile.components.BottomMediaMenuComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.InputFieldComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.StatefulProgressComponent
import com.mrgoodcat.hitmeup.presentation.ui.component.TopBarComponent
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    editProfileViewModel: EditProfileViewModel = hiltViewModel(),
) {
    val currentUser by editProfileViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var isProgressActive by remember {
        mutableStateOf(false)
    }

    var progressPercentage by remember {
        mutableStateOf(0)
    }
    var latch by remember { mutableIntStateOf(1) }
    var bottomSheetStateManual by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(key1 = bottomSheetStateManual) {
        if (bottomSheetStateManual) bottomSheetState.expand() else bottomSheetState.hide()
    }

    LaunchedEffect(key1 = Unit) {
        editProfileViewModel.updateNetworkStatus()
        editProfileViewModel.updateResult.collect {
            when (it) {
                is UpdateResult.Error -> {
                    isProgressActive = false
                }

                is UpdateResult.Loading -> {
                    isProgressActive = true
                    progressPercentage = it.percentage
                }

                is UpdateResult.Success -> {
                    isProgressActive = false
                    navController.popBackStack()
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopBarComponent(
                stringResource(id = R.string.edit_profile_screen_title),
                false,
                hasInternet = currentUser.hasInternet.value
            ) {
                Row(
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (latch == 0) return@IconButton
                            navController.popBackStack()
                            latch--
                        },
                        modifier = Modifier.width(60.dp)
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.cancel_button_title),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontFamily = Roboto,
                            color = LightBlueBorder
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White),
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight()
                .fillMaxWidth()
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .imePadding()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var uri: Uri? = null

                if (currentUser.avatarUri.value.isNotEmpty()) {
                    uri = Uri.parse(currentUser.avatarUri.value)
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri ?: currentUser.avatarString.value)
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

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!currentUser.hasInternet.value) {
                                return@clickable
                            }
                            bottomSheetStateManual = true
                        },
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_change_avatar),
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = stringResource(id = R.string.change_photo_button_title),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = Roboto,
                        color = LightBlueBorder
                    )
                }

                Spacer(modifier = Modifier.height(9.dp))

                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.first_name_field_name),
                    fieldText = currentUser.firstName.value,
                    fieldErrorText = currentUser.firstNameError.value,
                    isFieldError = currentUser.firstNameError.value.isNotEmpty(),
                    isRequired = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onValueChange = { editProfileViewModel.editModelField(StateParams.FirstName(it)) }
                )

                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.last_name_field_name),
                    fieldText = currentUser.lastName.value,
                    fieldErrorText = currentUser.lastNameError.value,
                    isFieldError = currentUser.lastNameError.value.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    onValueChange = { editProfileViewModel.editModelField(StateParams.LastName(it)) }
                )

                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.phone_field_name),
                    fieldText = currentUser.phone.value,
                    fieldErrorText = currentUser.phoneError.value,
                    isFieldError = currentUser.phoneError.value.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = { editProfileViewModel.editModelField(StateParams.Phone(it)) }
                )

                InputFieldComponent(
                    fieldTitle = stringResource(id = R.string.email_field_name),
                    fieldText = currentUser.email.value,
                    enabled = false,
                    fieldErrorText = "",
                    isFieldError = false,
                    isRequired = true,
                    onValueChange = { }
                )

                Spacer(modifier = Modifier.height(5.dp))

                Button(
                    onClick = {
                        if (!currentUser.hasInternet.value) {
                            return@Button
                        }
                        editProfileViewModel.updateProfileClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = LightBlueBorder)
                ) {
                    Text(
                        text = stringResource(id = R.string.update_profile_button_text),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color.White,
                        fontFamily = Roboto,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        if (bottomSheetState.isVisible) {
            BottomMediaMenuComponent(bottomSheetState, onDismissRequest = {
                bottomSheetStateManual = false
            }, onImagePickedUp = { imageUri ->
                imageUri?.let {
                    Timber.d("bottomComponent uri: $it")
                    editProfileViewModel.editModelField(StateParams.AvatarUri(it.toString()))
                    bottomSheetStateManual = false
                }
            })
        }
    }

    if (isProgressActive) {
        StatefulProgressComponent(progressPercentage, false)
    }
}