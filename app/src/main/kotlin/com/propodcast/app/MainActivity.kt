package com.propodcast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.analytics.AnalyticsTracker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var analytics: AnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProPodcastTheme {
                ProPodcastApp(onScreenView = analytics::logScreen)
            }
        }
    }
}
