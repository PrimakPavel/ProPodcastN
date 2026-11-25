package com.propodcast.app.feature.player

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.propodcast.app.core.commonwidgets.rememberSheetCloser

/** Full player in a full-height bottom sheet: swipe down or the ⌄ button collapses it back to the mini player. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    state: PlayerUiState,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onCycleSpeed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val collapse = rememberSheetCloser(sheetState, onDismiss)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        FullPlayerContent(
            state = state,
            onCollapse = collapse,
            onTogglePlayPause = onTogglePlayPause,
            onSeekTo = onSeekTo,
            onSeekBack = onSeekBack,
            onSeekForward = onSeekForward,
            onCycleSpeed = onCycleSpeed,
        )
    }
}
