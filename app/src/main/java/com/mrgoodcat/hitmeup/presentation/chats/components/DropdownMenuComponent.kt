package com.mrgoodcat.hitmeup.presentation.chats.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle

@Composable
fun DropdownMenuComponent(
    expanded: Boolean,
    menuItems: List<MenuParams>,
    dismissRequest: () -> Unit,
    onClickElement: (Int) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { dismissRequest() },
        offset = DpOffset(65.dp, 0.dp),
        shape = RoundedCornerShape(5.dp),
        containerColor = Color.White,
        modifier = Modifier.height(45.dp),
    ) {
        menuItems.mapIndexed { index, menuParams ->
            DropdownMenuItem(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(30.dp),
                contentPadding = PaddingValues(horizontal = 25.dp),
                text = {
                    Text(
                        text = menuParams.menuItemText,
                        color = BlackTitle,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    )
                },
                onClick = {
                    onClickElement(index)
                })
        }
    }
}

data class MenuParams(val menuItemText: String = "")