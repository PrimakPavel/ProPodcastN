package com.propodcast.app.feature.player.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.R

/** Play / pause toggle; a spinner surrounds it while the audio is buffering. [large] is for the full player. */
@Composable
internal fun PlayPauseButton(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val painter = if (isPlaying) painterResource(R.drawable.ic_pause) else rememberVectorPainter(Icons.Filled.PlayArrow)
    val description = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play)
    val buttonSize = if (large) 72.dp else 48.dp
    Box(modifier.size(buttonSize), contentAlignment = Alignment.Center) {
        if (large) {
            FilledIconButton(onClick = onClick, modifier = Modifier.size(buttonSize)) {
                Icon(painter, contentDescription = description, modifier = Modifier.size(40.dp))
            }
        } else {
            IconButton(onClick = onClick) {
                Icon(painter, contentDescription = description)
            }
        }
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (large) buttonSize else 36.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}
