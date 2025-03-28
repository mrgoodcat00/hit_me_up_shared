package com.mrgoodcat.hitmeup.presentation.splash

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.presentation.home.BaseViewModel
import timber.log.Timber

@Preview
@Composable
fun SplashScreen(
    baseViewModel: BaseViewModel = hiltViewModel(),
    onReady: () -> Unit = {}
) {

    LaunchedEffect(Unit) {
        Timber.d("SplashScreen LaunchedEffect ${baseViewModel.getListener()}")
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.White)
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Icon(
                tint = Color.Unspecified,
                painter = painterResource(id = R.drawable.ic_main_logo),
                contentDescription = "",
                modifier = Modifier
                    .width(128.dp)
                    .height(128.dp)
            )
        }
    }
}