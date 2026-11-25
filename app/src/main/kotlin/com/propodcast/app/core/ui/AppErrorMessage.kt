package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.toAppError

/** User-facing text for an [AppError]. */
@Composable
fun AppError.message(): String = when (this) {
    AppError.Unauthorized -> stringResource(R.string.error_unauthorized)
    AppError.NotFound -> stringResource(R.string.error_not_found)
    AppError.TooManyRequests -> stringResource(R.string.error_too_many_requests)
    is AppError.Client -> stringResource(R.string.error_client, code)
    is AppError.Server -> stringResource(R.string.error_server, code)
    AppError.Network -> stringResource(R.string.error_network)
    is AppError.Unknown -> stringResource(R.string.error_unknown)
}

/** User-facing text for any throwable coming from the data layer (e.g. a Paging LoadState.Error). */
@Composable
fun Throwable.userMessage(): String = toAppError().message()
