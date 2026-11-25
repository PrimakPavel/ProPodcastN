package com.propodcast.app.feature.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.propodcast.app.feature.player.widgets.rememberDragToDismiss

/**
 * Full-screen player over the whole app (navigation bar included). It slides up from the mini player while
 * the artwork flies between the two (shared element in [sharedTransitionScope]). Closes with back,
 * the ⌄ button or a pull down.
 */
@Composable
fun FullPlayerOverlay(
    visible: Boolean,
    state: PlayerUiState,
    sharedTransitionScope: SharedTransitionScope,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onCycleSpeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = visible, onBack = onCollapse)
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically { it / 4 } + fadeIn(),
        exit = slideOutVertically { it / 4 } + fadeOut(),
    ) {
        val artworkModifier = with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(PLAYER_ARTWORK_KEY),
                animatedVisibilityScope = this@AnimatedVisibility,
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .then(rememberDragToDismiss(onCollapse)),
            color = MaterialTheme.colorScheme.surface,
        ) {
            FullPlayerContent(
                state = state,
                onCollapse = onCollapse,
                onTogglePlayPause = onTogglePlayPause,
                onSeekTo = onSeekTo,
                onSeekBack = onSeekBack,
                onSeekForward = onSeekForward,
                onCycleSpeed = onCycleSpeed,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                artworkModifier = artworkModifier,
            )
        }
    }
}
