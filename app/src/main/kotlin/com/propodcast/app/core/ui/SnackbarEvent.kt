package com.propodcast.app.core.ui

import androidx.annotation.StringRes

/**
 * A snackbar a ViewModel asks the screen to show (see [SnackbarEffect]); the shared shape of every
 * "Removed · Undo" / "Cancelled · Retry" message. [onAction] runs when its action is tapped, [onDismiss]
 * when it goes away otherwise (timeout, swipe, ✕ or replaced by a newer snackbar).
 */
class SnackbarEvent(
    @param:StringRes val messageRes: Int,
    val messageArgs: List<Any> = emptyList(),
    @param:StringRes val actionLabelRes: Int? = null,
    val onAction: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
