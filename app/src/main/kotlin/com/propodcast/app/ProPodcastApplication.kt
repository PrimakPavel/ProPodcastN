package com.propodcast.app

import android.app.Application
import com.propodcast.app.widget.PlayerWidgetUpdater
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class ProPodcastApplication : Application() {

    @Inject
    lateinit var widgetUpdater: PlayerWidgetUpdater

    override fun onCreate() {
        super.onCreate()
        widgetUpdater.start()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
