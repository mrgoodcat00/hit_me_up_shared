package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.BottomNavigationItemModel
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.ChatsScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.ContactsScreen
import com.mrgoodcat.hitmeup.domain.model.Constants.HitMeUpScreen.ProfileScreen
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyBottomBarBackground
import com.mrgoodcat.hitmeup.presentation.ui.theme.GreyMenuText
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightBlueBorder

@Preview
@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    currentRoute: String = "",
    items: List<BottomNavigationItemModel> = listOf(
        BottomNavigationItemModel(
            name = stringResource(id = R.string.bottom_navigation_item_chats),
            route = ChatsScreen.route,
            icon = ImageVector.vectorResource(id = R.drawable.ic_menu_chats),
            badgeCount = 0
        ),
        BottomNavigationItemModel(
            name = stringResource(id = R.string.bottom_navigation_item_contacts),
            route = ContactsScreen.route,
            icon = ImageVector.vectorResource(id = R.drawable.ic_menu_contacts),
            badgeCount = 0
        ),
        BottomNavigationItemModel(
            name = stringResource(id = R.string.bottom_navigation_item_profile),
            route = ProfileScreen.route,
            icon = ImageVector.vectorResource(id = R.drawable.ic_menu_profile),
            badgeCount = 0
        ),
    ),
    onItemClick: (BottomNavigationItemModel) -> Unit = {}
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = GreyBottomBarBackground,
        tonalElevation = 3.dp
    ) {
        items.forEach { item ->
            val selected = item.isForcedSelect || item.route == currentRoute

            NavigationBarItem(
                modifier = Modifier
                    .height(80.dp),
                selected = selected,
                onClick = {
                    if (item.route === currentRoute) {
                        return@NavigationBarItem
                    }

                    onItemClick(item)
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = LightBlueBorder,
                    unselectedIconColor = GreyMenuText,
                    selectedTextColor = LightBlueBorder,
                    unselectedTextColor = GreyMenuText
                ),
                icon = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        if (item.badgeCount > 0) {
                            //Text(text = item.badgeCount.toString())
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .size(22.dp)
                            )
                        }

                        Text(
                            text = item.name,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 0.dp)
                        )
                    }
                },
            )
        }

    }
}


