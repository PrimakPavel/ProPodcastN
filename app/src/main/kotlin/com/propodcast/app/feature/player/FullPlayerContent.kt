package com.propodcast.app.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.PodcastImage
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.feature.player.widgets.PlayPauseButton
import com.propodcast.app.feature.player.widgets.PlayerSeekBar
import com.propodcast.app.feature.player.widgets.SkipButton
import com.propodcast.app.feature.player.widgets.SpeedButton
import com.propodcast.app.feature.player.widgets.playerSubtitle

/** Stateless full player: artwork, titles, seek bar, -15 / play / +30 and speed. Also used by UI tests. */
@OptIn(ExperimentalMaterial3Api::class) // BottomSheetDefaults.DragHandle
@Composable
fun FullPlayerContent(
    state: PlayerUiState,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onCycleSpeed: () -> Unit,
    modifier: Modifier = Modifier,
    /** Extra modifier for the artwork, e.g. a shared element transition from the mini player. */
    artworkModifier: Modifier = Modifier,
) {
    val playback = state.playback
    val item = playback.nowPlaying ?: return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .testTag(FULL_PLAYER_TAG),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BottomSheetDefaults.DragHandle(Modifier.align(Alignment.CenterHorizontally))
        Row(Modifier.fillMaxWidth()) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.player_collapse))
            }
        }
        PodcastImage(
            url = item.imageUrl,
            modifier = artworkModifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.85f)
                .aspectRatio(1f),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        playerSubtitle(state)?.let { subtitle ->
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (playback.error != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        PlayerSeekBar(positionMs = playback.positionMs, durationMs = playback.durationMs, onSeek = onSeekTo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkipButton(seconds = (PlayerController.SEEK_BACK_MS / 1_000).toInt(), forward = false, onClick = onSeekBack)
            PlayPauseButton(
                isPlaying = playback.isPlaying,
                isBuffering = playback.isBuffering,
                onClick = onTogglePlayPause,
                large = true,
            )
            SkipButton(seconds = (PlayerController.SEEK_FORWARD_MS / 1_000).toInt(), forward = true, onClick = onSeekForward)
        }
        Spacer(Modifier.height(16.dp))
        SpeedButton(speed = playback.speed, onClick = onCycleSpeed)
        Spacer(Modifier.height(24.dp))
    }
}
