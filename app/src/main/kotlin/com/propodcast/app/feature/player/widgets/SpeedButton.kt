package com.propodcast.app.feature.player.widgets

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.propodcast.app.R
import com.propodcast.app.core.ui.formatSpeed

/** Shows the current speed ("1.5×"); each tap switches to the next one. */
@Composable
internal fun SpeedButton(
    speed: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = formatSpeed(speed)
    val description = stringResource(R.string.player_speed, label)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description },
    ) {
        Text(label)
    }
}
