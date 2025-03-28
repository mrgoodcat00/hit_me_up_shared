package com.mrgoodcat.hitmeup.presentation.chats.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder

@Preview
@Composable
fun ConfirmationDialog(
    alertDialogParams: ConfirmationDialogParams = ConfirmationDialogParams(),
    dismiss: (() -> Unit)? = {},
    cancel: () -> Unit = {},
    confirm: () -> Unit = {}
) {

    AlertDialog(
        dismissButton = {
            Text(
                text = alertDialogParams.cancelButtonText.ifEmpty { stringResource(id = R.string.cancel_button_title) },
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = LightBlueBorder,
                modifier = Modifier
                    .clickable { cancel() }
                    .padding(end = 15.dp)
            )
        },
        confirmButton = {
            Text(
                text = alertDialogParams.confirmButtonText.ifEmpty { stringResource(id = R.string.confirm_button_title) },
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = LightBlueBorder,
                modifier = Modifier.clickable { confirm() })
        },
        title = {
            Text(
                text = alertDialogParams.title,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                color = BlackTitle,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = alertDialogParams.text,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                color = BlackTitle
            )
        },
        shape = RoundedCornerShape(15.dp),
        onDismissRequest = { dismiss },
        containerColor = Color.White
    )
}

data class ConfirmationDialogParams(
    var title: String = "",
    var text: String = "",
    var cancelButtonText: String = "",
    var confirmButtonText: String = "",
)