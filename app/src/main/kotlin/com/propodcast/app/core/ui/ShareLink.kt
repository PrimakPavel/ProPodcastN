package com.propodcast.app.core.ui

import android.content.Context
import android.content.Intent

/** Opens the system share sheet with [title] and [url] as plain text. */
fun Context.shareLink(title: String, url: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, url)
    }
    startActivity(Intent.createChooser(send, chooserTitle))
}
