package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightGreyBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarComponent(
    title: String = "",
    useDivider: Boolean = true,
    hasInternet: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column {
        CenterAlignedTopAppBar(
            expandedHeight = 48.dp,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(11.dp),
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    fontFamily = Roboto,
                    letterSpacing = 0.5.sp,
                    color = BlackTitle,
                    fontWeight = FontWeight.SemiBold
                )
            },
            actions = actions,
            colors = TopAppBarColors(
                containerColor = Color.White,
                scrolledContainerColor = Color.Green,
                navigationIconContentColor = Color.LightGray,
                titleContentColor = BlackTitle,
                actionIconContentColor = Color.Blue
            )
        )

        if (useDivider) {
            Divider(color = LightGreyBorder, thickness = 0.5.dp)
        }

        NoInternetMessageComponent(hasInternet = hasInternet)
    }
}