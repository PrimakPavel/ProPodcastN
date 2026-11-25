package com.propodcast.app.feature.player.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.propodcast.app.R

/** Circular arrow with the number of seconds inside, like "↺15" / "30↻". */
@Composable
internal fun SkipButton(
    seconds: Int,
    forward: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier.size(56.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(if (forward) R.drawable.ic_forward else R.drawable.ic_replay),
                contentDescription = stringResource(
                    if (forward) R.string.player_seek_forward else R.string.player_seek_back,
                    seconds,
                ),
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = seconds.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                // The circle's centre sits slightly below the icon's centre.
                modifier = Modifier
                    .offset(y = 2.dp)
                    .clearAndSetSemantics {},
            )
        }
    }
}
