package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Publication date in the user's locale and time zone, e.g. "Nov 14, 2016". */
fun formatPubDate(epochMs: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(zone).format(Instant.ofEpochMilli(epochMs))

/** "1 h 5 min" or "41 min"; empty when the duration is unknown. */
@Composable
fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return ""
    val totalMinutes = (seconds + 30) / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        stringResource(R.string.duration_hours_minutes, hours, minutes)
    } else {
        stringResource(R.string.duration_minutes, minutes.coerceAtLeast(1))
    }
}
