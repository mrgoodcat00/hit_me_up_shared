package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyMenuText

@Preview
@Composable
fun EmptyDataPlaceholderComponent(text: String = stringResource(id = R.string.no_items_default_text)) {
    Surface(
        modifier = Modifier.background(Color.White)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                color = GreyMenuText,
                modifier = Modifier.align(Alignment.Center),
                text = text
            )
        }
    }
}