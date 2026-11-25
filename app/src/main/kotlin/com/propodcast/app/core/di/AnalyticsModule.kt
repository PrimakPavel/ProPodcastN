package com.propodcast.app.core.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.analytics.FirebaseAnalyticsTracker
import com.propodcast.app.core.analytics.NoOpAnalyticsTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    /** Firebase only when the build had google-services.json (FirebaseApp was initialised). */
    @Provides
    @Singleton
    fun provideAnalyticsTracker(@ApplicationContext context: Context): AnalyticsTracker =
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseAnalyticsTracker(FirebaseAnalytics.getInstance(context))
        } else {
            NoOpAnalyticsTracker
        }
}
