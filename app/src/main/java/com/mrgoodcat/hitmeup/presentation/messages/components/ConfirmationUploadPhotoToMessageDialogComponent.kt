package com.mrgoodcat.hitmeup.presentation.messages.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.presentation.messages.MessagesViewModel
import com.mrgoodcat.hitmeup.presentation.messages.UploadResult
import com.mrgoodcat.hitmeup.presentation.ui.component.StatefulProgressComponent
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import timber.log.Timber

@Preview
@Composable
fun ConfirmationUploadPhotoToMessageDialogComponent(
    messageViewModel: MessagesViewModel = hiltViewModel<MessagesViewModel>(),
    uri: Uri = Uri.Builder().build(),
    dismiss: () -> Unit = {},
    cancel: () -> Unit = {},
    confirm: () -> Unit = {}
) {

    var isProgressActive by remember {
        mutableStateOf(false)
    }
    var progressPercentage by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(key1 = Unit) {
        messageViewModel.uploadResult.collect {
            Timber.d("confDialog collect: $it")
            when (it) {
                is UploadResult.Error -> {
                    dismiss()
                }

                is UploadResult.Loading -> {
                    isProgressActive = true
                    progressPercentage = it.percentage
                }

                is UploadResult.Success -> {
                    isProgressActive = false
                    progressPercentage = 0
                    dismiss()
                }
            }
        }
    }

    AlertDialog(
        dismissButton = {
            Text(
                text = stringResource(id = R.string.cancel_button_title),
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = LightBlueBorder,
                modifier = Modifier.clickable {
                    if (!isProgressActive)
                        cancel()
                })
        },
        confirmButton = {
            Text(
                text = stringResource(id = R.string.confirm_button_title),
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = LightBlueBorder,
                modifier = Modifier.clickable {
                    if (!isProgressActive)
                        confirm()
                })
        },
        title = {
            Text(
                text = stringResource(id = R.string.send_this_image_to_chat),
                fontSize = 18.sp,
                lineHeight = 20.sp,
                color = BlackTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .error(R.drawable.ic_add_image)
                        .build(),
                    placeholder = painterResource(id = R.drawable.fui_ic_anonymous_white_24dp),
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
            if (isProgressActive) {
                StatefulProgressComponent(progressPercentage, false)
            }
        },
        shape = RoundedCornerShape(15.dp),
        onDismissRequest = { dismiss() },
        containerColor = Color.White,
        modifier = Modifier
            .width(350.dp)
            .height(450.dp)
    )
}