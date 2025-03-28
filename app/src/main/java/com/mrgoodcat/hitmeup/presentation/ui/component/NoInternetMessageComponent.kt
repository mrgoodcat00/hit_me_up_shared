package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyMenuText
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Preview
@Composable
fun NoInternetMessageComponent(
    modifier: Modifier = Modifier,
    hasInternet: Boolean = true,
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .animateContentSize()
            .height(if (hasInternet) 0.dp else 32.dp)
            .background(GreyMenuText),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.common_error_no_internet),
            color = Color.White,
            modifier = Modifier,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            fontFamily = Roboto
        )
    }
}