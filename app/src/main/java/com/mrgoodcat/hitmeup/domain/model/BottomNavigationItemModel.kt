package com.mrgoodcat.hitmeup.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItemModel(
    val name: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
    val isForcedSelect: Boolean = false
)