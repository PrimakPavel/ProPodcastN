package com.propodcast.app.core.ui

import android.content.Context
import android.text.format.Formatter
import com.propodcast.app.R
import kotlin.math.ceil

/**
 * Rough download size from the duration: Listen Notes has no file sizes, most podcasts are ~128 kbit/s
 * (about 1 MB a minute). Rounded up to tens of MB; null when the duration is unknown.
 */
fun estimatedDownloadMb(durationSec: Int): Int? {
    if (durationSec <= 0) return null
    val megabytes = durationSec * TYPICAL_BITRATE_BPS / 8.0 / 1_000_000
    return (ceil(megabytes / 10) * 10).toInt()
}

/** "< 10 MB", "60 MB", or null when unknown. */
fun Context.estimatedDownloadSizeLabel(durationSec: Int): String? = estimatedDownloadMb(durationSec)?.let { mb ->
    if (mb <= SMALL_MB) getString(R.string.download_size_small) else getString(R.string.download_size_mb, mb)
}

/** A real file size, e.g. "62 MB". */
fun Context.fileSizeLabel(bytes: Long): String = Formatter.formatShortFileSize(this, bytes)

private const val TYPICAL_BITRATE_BPS = 128_000
private const val SMALL_MB = 10
