package com.mrgoodcat.hitmeup.presentation.ui.component

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mrgoodcat.hitmeup.R
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun getFormattedTimeDate(timestamp: Long): String {
    val timeFormat = SimpleDateFormat(
        stringResource(id = R.string.time_format_HH_mm),
        Locale.getDefault()
    ).format(timestamp)

    val dateFormat = SimpleDateFormat(
        stringResource(id = R.string.date_format_with_dots_dd_MM_yyyy),
        Locale.getDefault()
    ).format(timestamp)

    return stringResource(
        id = R.string.last_seen_text_format,
        if (timestamp == 0L) {
            stringResource(id = R.string.long_time_ago)
        } else if (DateUtils.isToday(timestamp)) {
            " today at $timeFormat"
        } else {
            "on $dateFormat at $timeFormat"
        }
    )
}