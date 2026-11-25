package com.propodcast.app.feature.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.propodcast.app.core.model.EpisodeDownloadState
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
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
    downloadState: EpisodeDownloadState? = null,
    onDownloadClick: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
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
            Box {
            FullPlayerContent(
                state = state,
                onCollapse = onCollapse,
                onTogglePlayPause = onTogglePlayPause,
                onSeekTo = onSeekTo,
                onSeekBack = onSeekBack,
                onSeekForward = onSeekForward,
                onSpeedSelected = onSpeedSelected,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                artworkModifier = artworkModifier,
                downloadState = downloadState,
                onDownloadClick = onDownloadClick,
            )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                )
            }
        }
    }
}
