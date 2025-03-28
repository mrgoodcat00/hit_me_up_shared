package com.mrgoodcat.hitmeup.presentation.messages.components

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.utils.ImageUtils
import com.mrgoodcat.hitmeup.presentation.findActivity
import com.mrgoodcat.hitmeup.presentation.messages.MessagesViewModel
import com.mrgoodcat.hitmeup.presentation.messages.UploadResult
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyBottomBarBackground
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


@Composable
fun BottomSendMessageComponent(
    modifier: Modifier = Modifier,
    messageViewModel: MessagesViewModel = hiltViewModel<MessagesViewModel>(),
    chatId: String = "",
    hasInternet: Boolean = true,
) {

    val context = LocalContext.current

    var permissionErrorMessage by remember {
        mutableStateOf("")
    }

    var cameraUriState by remember {
        mutableStateOf<Uri?>(null)
    }
    var openUploadDialog by remember {
        mutableStateOf(false)
    }

    val messageText = remember {
        mutableStateOf("")
    }
    val buttonsAppeared = remember {
        derivedStateOf { messageText.value.isEmpty() }
    }
    val inputFieldLong = remember {
        derivedStateOf { messageText.value.isNotEmpty() }
    }

    LaunchedEffect(key1 = Unit) {
        messageViewModel.uploadResult.collect {
            Timber.d("bottomComponent collect:$it")
            when (it) {
                is UploadResult.Error -> {
                    permissionErrorMessage = it.message
                }

                is UploadResult.Loading -> {}

                is UploadResult.Success -> {
                    cameraUriState = null
                    openUploadDialog = false
                }
            }
        }
    }

    val camera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isImageSaved ->
            Timber.d("isImageSaved:${isImageSaved} file:${cameraUriState}")
            if (isImageSaved) {
                openUploadDialog = true
            }
        }

    val gallery =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            Timber.d("rememberLauncherForActivityResult: $it")
            if (it != null) {
                cameraUriState = it
                openUploadDialog = true
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                cameraUriState = ImageUtils.createTempFile(context)

                cameraUriState?.let {
                    camera.launch(it)
                }
            } else {
                permissionErrorMessage = context.getString(R.string.no_camera_access_error_message)
            }
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
        )
    ) {
        OutlinedTextField(
            value = messageText.value,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            onValueChange = {
                messageText.value = it
            },
            placeholder = {
                Text(
                    stringResource(id = R.string.send_message_placeholder),
                    color = BlackTitle,
                    fontFamily = Roboto,
                    fontSize = 15.sp,
                    lineHeight = 18.sp
                )
            },
            textStyle = TextStyle(
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontFamily = Roboto,
                color = BlackTitle,
            ),
            singleLine = true,
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                focusedTextColor = BlackTitle,
                cursorColor = LightBlueBorder,
                selectionColors = TextSelectionColors(
                    handleColor = LightBlueBorder,
                    backgroundColor = Color.LightGray
                ),
            ),
            modifier = Modifier
                .animateContentSize(alignment = Alignment.TopEnd)
                .fillMaxWidth(if (inputFieldLong.value.not()) 0.595f else 0.90f)
                .background(Color.White),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(155.dp)
                .padding(end = 15.dp),
            horizontalArrangement = Arrangement.End,
        ) {

            AnimatedVisibility(visible = buttonsAppeared.value) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .padding(end = 15.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (!hasInternet) {
                                return@IconButton
                            }
                            gallery.launch("image/*")

                        },
                        modifier = Modifier.width(36.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_add_image),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.size(10.dp)
                    )

                    IconButton(
                        onClick = {
                            if (!hasInternet) {
                                return@IconButton
                            }

                            when {
                                ContextCompat.checkSelfPermission(context, CAMERA)
                                        == PackageManager.PERMISSION_GRANTED -> {

                                    Timber.d("granted")

                                    cameraUriState = ImageUtils.createTempFile(context)

                                    cameraUriState?.let {
                                        camera.launch(
                                            it,
                                            ActivityOptionsCompat.makeTaskLaunchBehind()
                                        )
                                    }
                                }

                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    context.findActivity(), CAMERA
                                ) -> {
                                    Timber.d("shouldShowRequestPermissionRationale")
                                    permissionErrorMessage =
                                        context.getString(R.string.no_camera_access_error_message)
                                }

                                else -> {
                                    permissionLauncher.launch(CAMERA)
                                }
                            }
                        },
                        modifier = Modifier.width(36.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_add_photo),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    if (!hasInternet || messageText.value.trim().isEmpty()) {
                        return@IconButton
                    }

                    messageViewModel.sendTextMessage(messageText.value, chatId)
                    messageText.value = ""
                    messageViewModel.scrollToEnd()

                },
                modifier = Modifier.requiredSize(49.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_send_message),
                    contentDescription = "",
                    modifier = Modifier.size(37.dp, 37.dp)
                )
            }
        }
    }

    if (openUploadDialog) {
        cameraUriState?.let {
            ConfirmationUploadPhotoToMessageDialogComponent(
                messageViewModel,
                uri = it,
                cancel = {
                    Timber.d("before remove $cameraUriState")

                    cameraUriState?.let { uri ->
                        val deletedCount = ImageUtils.deleteTempFile(uri, context)
                        Timber.d("removed $deletedCount items")
                    }
                    openUploadDialog = false
                    cameraUriState = null
                },
                confirm = {
                    cameraUriState?.let { uri ->
                        messageViewModel.sendImageMessage(uri, chatId)
                    }
                }
            )
        }
    }

    ErrorMessages(permissionErrorMessage) {
        permissionErrorMessage = ""
    }

    Spacer(modifier = Modifier.height(25.dp))
}

@Composable
private fun ErrorMessages(error: String, isHidden: () -> Unit) {
    if (error.isNotEmpty()) {
        LaunchedEffect(key1 = Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(4000)
                isHidden()
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier.padding(horizontal = 15.dp)
        ) {
            Text(
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                fontSize = 15.sp,
                color = BlackTitle,
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreyBottomBarBackground)
                    .padding(vertical = 10.dp, horizontal = 10.dp)
            )
        }
    }
}