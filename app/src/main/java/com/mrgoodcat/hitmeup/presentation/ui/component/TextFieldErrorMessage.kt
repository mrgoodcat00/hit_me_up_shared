package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.presentation.ui.theme.ErrorBorderColor
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Composable
fun TextFieldErrorMessage(errorText: String) {
    Row(
        modifier = Modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_error_warning),
            contentDescription = "",
            modifier = Modifier
                .width(24.dp)
                .height(24.dp)
        )
        Text(
            text = errorText,
            color = ErrorBorderColor,
            fontFamily = Roboto,
            fontSize = 12.sp,
            lineHeight = 24.sp
        )
    }
}