package com.mrgoodcat.hitmeup.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mrgoodcat.hitmeup.R
import com.mrgoodcat.hitmeup.domain.model.FriendModel
import com.mrgoodcat.hitmeup.presentation.ui.theme.BlackTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.GrayTitle
import com.mrgoodcat.hitmeup.presentation.ui.theme.LightGreyBorder
import com.mrgoodcat.hitmeup.presentation.ui.theme.Pink80
import com.mrgoodcat.hitmeup.presentation.ui.theme.Roboto

@Preview
@Composable
fun ContactsListItemComponent(
    element: FriendModel = FriendModel(),
    isLastElement: Boolean = false,
    onStartChat: (FriendModel) -> Unit = {},
    onPreviewProfile: (FriendModel) -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 13.dp, end = 15.dp, start = 15.dp),
    ) {
        Column(
            modifier = Modifier
                .requiredWidth(48.dp)
                .padding(end = 10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(38.dp)
                    .height(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(Color.Green, Color.Yellow),
                            Offset.Infinite
                        ), shape = CircleShape, 0.6F
                    )
                    .clickable {
                        onPreviewProfile(element)
                    }
            ) {
                if (element.userAvatar.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(element.userAvatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_menu_profile),
                        modifier = Modifier
                            .width(38.dp)
                            .height(38.dp)
                            .background(Pink80, shape = CircleShape)
                            .clip(CircleShape),
                    )
                } else {
                    val fName =
                        if (element.userFirstName.length > 1) element.userFirstName.substring(0, 1)
                        else ""
                    val lName =
                        if (element.userLastName.length > 1) element.userLastName.substring(0, 1)
                        else ""

                    Text(
                        text = "$fName$lName".toUpperCase(Locale.current),
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        color = Color.White,
                        modifier = Modifier
                    )
                }
            }
        }

        Column(
            modifier = Modifier.clickable {
                onStartChat(element)
            },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${element.userFirstName} ${element.userLastName}",
                fontSize = 16.sp,
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                color = BlackTitle,
                lineHeight = 24.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 13.dp),
                text = getFormattedTimeDate(timestamp = element.userLastSeen),
                lineHeight = 14.sp,
                fontFamily = Roboto,
                fontSize = 14.sp,
                overflow = TextOverflow.Ellipsis,
                color = GrayTitle,
                maxLines = 1,
            )

            if (!isLastElement) {
                Divider(color = LightGreyBorder, thickness = 0.5.dp)
            }
        }
    }
}