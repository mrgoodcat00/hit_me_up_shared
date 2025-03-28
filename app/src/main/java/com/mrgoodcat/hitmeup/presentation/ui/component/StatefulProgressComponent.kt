package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.WhiteTransparentTitle
import kotlinx.coroutines.delay

@Preview
@Composable
fun StatefulProgressComponent(startProgress: Int = 0, isEndless: Boolean = true) {

    var progressPercentage by remember {
        mutableStateOf(0F)
    }

    if (!isEndless) {
        LaunchedEffect(key1 = startProgress) {
            loadProgress(startProgress) {
                progressPercentage = it
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(WhiteTransparentTitle)
            .pointerInput(Unit) {}
    ) {
        if (!isEndless) {
            CircularProgressIndicator(
                progress = { progressPercentage },
                modifier = Modifier.width(64.dp),
                color = LightBlueBorder
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = LightBlueBorder
            )
        }
    }
}

suspend fun loadProgress(progressStart: Int = 1, updateProgress: (Float) -> Unit) {
    for (i in progressStart..100) {
        updateProgress(i.toFloat() / 100)
        delay(100)
    }
}