package com.propodcast.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.propodcast.app.core.crash.CrashlyticsTree
import com.propodcast.app.widget.PlayerWidgetUpdater
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class ProPodcastApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var widgetUpdater: PlayerWidgetUpdater

    override fun onCreate() {
        super.onCreate()
        widgetUpdater.start()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Only when the build had google-services.json.
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            Timber.plant(CrashlyticsTree(FirebaseCrashlytics.getInstance()))
        }
    }

    /** Lets WorkManager create @HiltWorker workers (the default initializer is removed in the manifest). */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
