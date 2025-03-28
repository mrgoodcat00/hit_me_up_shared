package com.mrgoodcat.hitmeup.presentation.edit_profile.components

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.utils.ImageUtils
import com.mrgoodcat.hitmeup.presentation.findActivity
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyBottomBarBackground
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import kotlinx.coroutines.delay
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BottomMediaMenuComponent(
    bottomState: SheetState = rememberModalBottomSheetState(),
    onDismissRequest: () -> Unit = {},
    onImagePickedUp: (Uri?) -> Unit = {}
) {
    val context = LocalContext.current
    var permissionErrorMessage by remember {
        mutableStateOf("")
    }
    var cameraUriState by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    val camera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isImageSaved ->
            Timber.d("isImageSaved:${isImageSaved} file:${cameraUriState}")
            if (isImageSaved && cameraUriState != null) {
                onImagePickedUp(cameraUriState)
            }
        }

    val gallery =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            Timber.d("rememberLauncherForActivityResult: $it")
            if (it != null) {
                onImagePickedUp(it)
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

    ModalBottomSheet(
        containerColor = Color.White,
        onDismissRequest = {
            ImageUtils.deleteTempFile(cameraUriState, context)
            onDismissRequest()
        },
        sheetState = bottomState,
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .animateContentSize()
                .height(if (permissionErrorMessage.isNotEmpty()) 120.dp else 100.dp)
                .padding(horizontal = 0.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        when {
                            ContextCompat.checkSelfPermission(context, CAMERA)
                                    == PackageManager.PERMISSION_GRANTED -> {
                                cameraUriState = ImageUtils.createTempFile(context)
                                Timber.d("granted $cameraUriState")
                                cameraUriState?.let {
                                    camera.launch(it)
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
                    }
                ) {
                    Icon(
                        tint = LightBlueBorder,
                        painter = painterResource(id = R.drawable.ic_change_avatar),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(id = R.string.camera_button_title),
                        lineHeight = 24.sp,
                        fontSize = 16.sp,
                        color = LightBlueBorder,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        gallery.launch("image/*")
                    }
                ) {
                    Icon(
                        tint = LightBlueBorder,
                        painter = painterResource(id = R.drawable.ic_add_image),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(id = R.string.gallery_button_title),
                        lineHeight = 24.sp,
                        fontSize = 16.sp,
                        color = LightBlueBorder,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (permissionErrorMessage.isNotEmpty()) {
                showAnError(permissionErrorMessage) {
                    permissionErrorMessage = ""
                }
            }
        }
    }
}

@Composable
private fun showAnError(error: String, isHidden: () -> Unit) {
    if (error.isNotEmpty()) {
        LaunchedEffect(key1 = Unit) {
            delay(4000)
            isHidden()
        }
        Spacer(modifier = Modifier.height(15.dp))
        Row(modifier = Modifier) {
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
