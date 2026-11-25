package com.propodcast.app.feature.home.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.propodcast.app.core.commonwidgets.ListFooterError
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.userMessage

/** Loading spinner or retryable error after the last loaded page. */
internal fun LazyGridScope.appendFooter(podcasts: LazyPagingItems<Podcast>) {
    when (val append = podcasts.loadState.append) {
        is LoadState.Loading -> item(key = "append-loading", span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> item(key = "append-error", span = { GridItemSpan(maxLineSpan) }) {
            ListFooterError(message = append.error.userMessage(), onRetry = podcasts::retry)
        }
        is LoadState.NotLoading -> Unit
    }
}
