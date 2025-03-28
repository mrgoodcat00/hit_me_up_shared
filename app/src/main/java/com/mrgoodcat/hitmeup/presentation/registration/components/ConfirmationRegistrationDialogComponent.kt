package com.mrgoodcat.hitmeup.presentation.registration.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.data.model.UserLocalModel
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightGreyBorder

@Preview
@Composable
fun ConfirmationRegistrationDialogComponent(
    userModel: UserLocalModel = UserLocalModel(),
    cancel: () -> Unit = {},
    authorizeWithCurrentUser: (UserLocalModel) -> Unit = {},
    createAccount: (UserLocalModel) -> Unit = {}
) {

    AlertDialog(
        dismissButton = {
            Text(
                text = stringResource(id = R.string.cancel_button_title),
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = LightBlueBorder,
                modifier = Modifier.clickable { cancel() })
        },
        confirmButton = {},
        title = {
            Text(
                text = stringResource(id = R.string.already_have_account_confirmation_text),
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = BlackTitle,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clickable {
                            authorizeWithCurrentUser(userModel)
                        }
                        .border(
                            border = BorderStroke(1.dp, LightGreyBorder),
                            shape = RoundedCornerShape(5.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Spacer(modifier = Modifier.width(7.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userModel.user_avatar)
                            .crossfade(true)
                            .error(R.drawable.fui_ic_anonymous_white_24dp)
                            .build(),
                        placeholder = painterResource(id = R.drawable.fui_ic_anonymous_white_24dp),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(44.dp)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(Color.LightGray, Color.DarkGray),
                                    Offset.Infinite
                                ), shape = CircleShape, 0.5F
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.77F)
                            .padding(vertical = 7.dp)
                    ) {
                        Text(
                            text = "${userModel.user_first_name} ${userModel.user_last_name}",
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = BlackTitle,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = userModel.user_email,
                            fontSize = 14.sp,
                            lineHeight = 24.sp,
                            color = GrayTitle,
                            fontWeight = FontWeight.Normal,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_forward_arrow),
                        tint = Color.Unspecified,
                        contentDescription = "",
                        modifier = Modifier
                            .size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { createAccount(userModel) },
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(1.dp, LightBlueBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.create_new_account_button_title),
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightBlueBorder,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

        },
        shape = RoundedCornerShape(15.dp),
        onDismissRequest = { },
        containerColor = Color.White
    )
}