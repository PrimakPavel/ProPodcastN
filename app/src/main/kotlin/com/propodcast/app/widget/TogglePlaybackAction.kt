package com.propodcast.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.propodcast.app.core.model.toNowPlaying
import kotlinx.coroutines.flow.first

/** Widget play / pause. With nothing loaded it continues the saved episode from its saved position. */
class TogglePlaybackAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = context.playerWidgetEntryPoint()
        val controller = entryPoint.playerController()
        if (controller.state.value.nowPlaying != null) {
            controller.togglePlayPause()
        } else {
            entryPoint.userPreferences().lastPlayed.first()?.let { last ->
                controller.play(last.toNowPlaying(), last.positionMs)
            }
        }
    }
}
