package com.propodcast.app.feature.player.widgets

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Pull the content down to dismiss it, like a bottom sheet. Works with scrollable content inside:
 * the pull starts only once the content is scrolled to the top. Released past [threshold] or with a
 * fast downward fling it calls [onDismiss], otherwise it springs back.
 */
@Composable
internal fun rememberDragToDismiss(onDismiss: () -> Unit): Modifier {
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val threshold = with(LocalDensity.current) { DISMISS_DISTANCE.toPx() }
    val minFlingVelocity = with(LocalDensity.current) { DISMISS_FLING_VELOCITY.toPx() }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Dragging back up while pulled down: undo the pull before the content scrolls.
                if (available.y < 0 && offsetY > 0f && source == NestedScrollSource.UserInput) {
                    val consumed = maxOf(available.y, -offsetY)
                    offsetY += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Content is at its top and the finger keeps going down: pull the whole player.
                if (available.y > 0 && source == NestedScrollSource.UserInput) {
                    offsetY += available.y
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offsetY <= 0f) return Velocity.Zero
                if (offsetY > threshold || available.y > minFlingVelocity) {
                    currentOnDismiss() // the exit animation continues from the current offset
                } else {
                    scope.launch { animate(offsetY, 0f) { value, _ -> offsetY = value } }
                }
                return available
            }
        }
    }
    return Modifier
        .nestedScroll(connection)
        .graphicsLayer { translationY = offsetY }
}

private val DISMISS_DISTANCE = 120.dp
private val DISMISS_FLING_VELOCITY = 1_000.dp // per second
