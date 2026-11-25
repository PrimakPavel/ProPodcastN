package com.propodcast.app.core.crash

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Sends warnings and errors logged through Timber to Crashlytics: as log lines attached to the next report,
 * and errors with a throwable as non-fatal issues. Crashes are reported by Crashlytics itself.
 */
class CrashlyticsTree(private val crashlytics: FirebaseCrashlytics) : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        crashlytics.log("${if (priority >= Log.ERROR) "E" else "W"}/${tag.orEmpty()}: $message")
        if (t != null && priority >= Log.ERROR) crashlytics.recordException(t)
    }
}
