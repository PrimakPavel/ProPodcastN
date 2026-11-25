package com.propodcast.app.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

/** Home screen widget with the current or last episode (stage 7). */
class PlayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = context.playerWidgetEntryPoint()
        val controller = entryPoint.playerController()
        val preferences = entryPoint.userPreferences()
        val initial = playerWidgetState(controller.state.value, preferences.lastPlayed.first())

        provideContent {
            // While the session lives it follows the player itself; PlayerWidgetUpdater restarts it otherwise.
            val stateFlow = remember {
                combine(controller.state, preferences.lastPlayed, ::playerWidgetState).distinctUntilChanged()
            }
            val state by stateFlow.collectAsState(initial)
            val artwork by produceState<Bitmap?>(initialValue = null, state?.imageUrl) {
                value = state?.imageUrl?.let { loadWidgetArtwork(context, it) }
            }
            GlanceTheme {
                PlayerWidgetContent(state, artwork)
            }
        }
    }
}
