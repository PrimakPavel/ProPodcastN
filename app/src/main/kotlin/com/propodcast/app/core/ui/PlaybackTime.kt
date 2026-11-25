package com.propodcast.app.core.ui

/** Player clock: "4:05", "12:30" or "1:02:03". Negative values count as 0. */
fun formatPlaybackTime(ms: Long): String {
    val totalSec = ms.coerceAtLeast(0) / 1_000
    val hours = totalSec / 3_600
    val minutes = totalSec % 3_600 / 60
    val seconds = totalSec % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

/** Playback speed label: "1×", "1.25×", "0.75×". */
fun formatSpeed(speed: Float): String =
    "%s×".format(speed.toBigDecimal().stripTrailingZeros().toPlainString())
