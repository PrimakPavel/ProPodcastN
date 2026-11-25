package com.propodcast.app.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.toNowPlaying
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.core.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Activity-wide player UI: the live player, or the episode to continue from the last session. */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlayerController,
    preferences: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = combine(controller.state, preferences.lastPlayed) { playback, last ->
        when {
            playback.nowPlaying != null -> PlayerUiState(playback)
            last != null -> PlayerUiState(
                playback = PlayerState(
                    nowPlaying = last.toNowPlaying(),
                    positionMs = last.positionMs,
                    durationMs = last.durationMs,
                    speed = playback.speed,
                ),
                isRestored = true,
            )
            else -> PlayerUiState()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlayerUiState())

    /**
     * Plays [episode]; the episode already in the player is paused / resumed instead of restarted.
     * Returns false when the episode has no audio file.
     */
    fun play(episode: Episode): Boolean {
        val item = episode.toNowPlaying() ?: return false
        val state = uiState.value
        val current = state.playback.nowPlaying
        when {
            current?.episodeId != item.episodeId -> controller.play(item)
            state.isRestored -> controller.play(item, state.playback.positionMs)
            else -> controller.togglePlayPause()
        }
        return true
    }

    fun togglePlayPause() {
        val state = uiState.value
        if (state.isRestored) loadRestored(state.playback.positionMs) else controller.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        if (uiState.value.isRestored) loadRestored(positionMs) else controller.seekTo(positionMs)
    }

    fun seekBack() {
        val state = uiState.value
        if (state.isRestored) {
            loadRestored((state.playback.positionMs - PlayerController.SEEK_BACK_MS).coerceAtLeast(0))
        } else {
            controller.seekBack()
        }
    }

    fun seekForward() {
        val state = uiState.value
        if (state.isRestored) {
            loadRestored(state.playback.positionMs + PlayerController.SEEK_FORWARD_MS)
        } else {
            controller.seekForward()
        }
    }

    /** 1× → 1.25× → 1.5× → 1.75× → 2× → 0.75× → 1×. */
    fun cycleSpeed() {
        val current = uiState.value.playback.speed
        val index = SPEED_CYCLE.indexOfFirst { abs(it - current) < SPEED_EPSILON }
        // An unknown speed (index -1) goes back to 1×.
        controller.setSpeed(if (index < 0) 1f else SPEED_CYCLE[(index + 1) % SPEED_CYCLE.size])
    }

    fun stop() = controller.stop()

    private fun loadRestored(positionMs: Long) {
        uiState.value.playback.nowPlaying?.let { controller.play(it, positionMs) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        val SPEED_CYCLE = listOf(1f, 1.25f, 1.5f, 1.75f, 2f, 0.75f)
        const val SPEED_EPSILON = 0.01f
    }
}
