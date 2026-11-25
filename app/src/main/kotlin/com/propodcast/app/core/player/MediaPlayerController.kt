package com.propodcast.app.core.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.NowPlaying
import com.propodcast.app.core.model.toLastPlayed
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * [PlayerController] backed by a Media3 [MediaController] connected to [PlaybackService].
 * Lives as long as the process, so it also saves the last episode and position while the app is in background.
 */
@Singleton
class MediaPlayerController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: UserPreferencesRepository,
) : PlayerController {

    // MediaController must be used on the main thread.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectMutex = Mutex()
    private var controller: MediaController? = null
    private var progressJob: Job? = null

    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish(player)
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) onIsPlayingChanged(player)
            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) && player.playbackState == Player.STATE_ENDED) {
                // Finished: next time "continue" starts from the beginning.
                scope.launch { preferences.updateLastPlayedPosition(0) }
            }
        }
    }

    override fun play(item: NowPlaying, startPositionMs: Long) {
        withController {
            setMediaItem(item.toMediaItem(), startPositionMs)
            prepare()
            play()
            preferences.setLastPlayed(item.toLastPlayed(startPositionMs))
        }
    }

    override fun togglePlayPause() = withController {
        when {
            playWhenReady && playbackState != Player.STATE_ENDED && playerError == null -> pause()
            playbackState == Player.STATE_ENDED -> {
                seekTo(0)
                play()
            }
            else -> {
                // After an error the player is idle and must be prepared again.
                if (playbackState == Player.STATE_IDLE) prepare()
                play()
            }
        }
    }

    override fun seekTo(positionMs: Long) = withController { seekTo(positionMs.coerceAtLeast(0)) }

    override fun seekBack() = withController { seekBack() }

    override fun seekForward() = withController { seekForward() }

    override fun setSpeed(speed: Float) = withController { setPlaybackSpeed(speed) }

    override fun stop() {
        withController {
            stop()
            clearMediaItems()
            preferences.clearLastPlayed()
        }
    }

    private fun withController(action: suspend MediaController.() -> Unit) {
        scope.launch {
            try {
                connectedController().action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Player action failed")
            }
        }
    }

    private suspend fun connectedController(): MediaController = connectMutex.withLock {
        controller ?: MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java)),
        ).buildAsync().await().also { connected ->
            connected.addListener(listener)
            controller = connected
            publish(connected)
            onIsPlayingChanged(connected)
        }
    }

    private fun publish(player: Player) {
        val nowPlaying = player.currentMediaItem?.toNowPlaying()
        mutableState.value = PlayerState(
            nowPlaying = nowPlaying,
            isPlaying = player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playerError == null,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            positionMs = player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: nowPlaying?.durationMs ?: 0,
            speed = player.playbackParameters.speed,
            error = player.playerError?.toPlayerError(),
        )
    }

    /** While playing: refresh the position for the UI and save it now and then. */
    private fun onIsPlayingChanged(player: Player) {
        progressJob?.cancel()
        if (player.isPlaying) {
            progressJob = scope.launch {
                var ticks = 0
                while (isActive) {
                    delay(PROGRESS_INTERVAL_MS)
                    publish(player)
                    if (++ticks % SAVE_EVERY_TICKS == 0) savePosition(player)
                }
            }
        } else if (player.currentMediaItem != null && player.playbackState != Player.STATE_ENDED) {
            scope.launch { savePosition(player) }
        }
    }

    private suspend fun savePosition(player: Player) {
        preferences.updateLastPlayedPosition(player.currentPosition.coerceAtLeast(0))
    }

    private fun PlaybackException.toPlayerError(): PlayerError = when (errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        -> PlayerError.NETWORK
        else -> PlayerError.SOURCE
    }

    private companion object {
        const val PROGRESS_INTERVAL_MS = 500L
        const val SAVE_EVERY_TICKS = 10 // every 5 s
    }
}
