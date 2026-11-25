package com.propodcast.app.feature.player.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.player.PlayerError
import com.propodcast.app.feature.player.PlayerUiState

/** Line under the title: the error, "Continue listening" for a restored episode, or the podcast name. */
@Composable
internal fun playerSubtitle(state: PlayerUiState): String? = when {
    state.playback.error == PlayerError.NETWORK -> stringResource(R.string.player_error_network)
    state.playback.error == PlayerError.SOURCE -> stringResource(R.string.player_error_source)
    state.isRestored -> stringResource(R.string.player_continue)
    else -> state.playback.nowPlaying?.podcastTitle
}
