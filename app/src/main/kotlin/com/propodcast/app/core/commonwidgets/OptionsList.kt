package com.propodcast.app.core.commonwidgets

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.ui.OptionsState
import com.propodcast.app.core.ui.message

/** Loading / error with retry / loaded list of filter options. */
@Composable
fun <T> OptionsList(
    options: OptionsState<T>,
    onRetry: () -> Unit,
    modifier: Modifier,
    content: LazyListScope.(List<T>) -> Unit,
) {
    when (options) {
        OptionsState.Loading -> LoadingState(modifier)
        is OptionsState.Error -> MessageState(
            message = stringResource(R.string.filters_load_error) + "\n" + options.error.message(),
            onRetry = onRetry,
            modifier = modifier,
        )
        is OptionsState.Loaded -> LazyColumn(modifier) { content(options.items) }
    }
}
