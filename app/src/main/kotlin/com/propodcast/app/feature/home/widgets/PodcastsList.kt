package com.propodcast.app.feature.home.widgets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.LoadingState
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.commonwidgets.PodcastListItem
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.userMessage
import com.propodcast.app.feature.home.PODCASTS_LIST_TAG

/** Paged best-podcasts grid with loading / error / empty states and pull to refresh. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PodcastsList(
    podcasts: LazyPagingItems<Podcast>,
    favoriteIds: Set<String>,
    onPodcastClick: (Podcast) -> Unit,
    onToggleFavorite: (Podcast) -> Unit,
    onShare: (Podcast) -> Unit,
) {
    val refresh = podcasts.loadState.refresh
    when {
        podcasts.itemCount == 0 && refresh is LoadState.Loading -> LoadingState()
        podcasts.itemCount == 0 && refresh is LoadState.Error ->
            MessageState(message = refresh.error.userMessage(), onRetry = podcasts::retry)
        podcasts.itemCount == 0 && refresh is LoadState.NotLoading && podcasts.loadState.append.endOfPaginationReached ->
            MessageState(message = stringResource(R.string.home_empty), onRetry = podcasts::refresh)
        else -> PullToRefreshBox(
            isRefreshing = refresh is LoadState.Loading,
            onRefresh = podcasts::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 360.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(PODCASTS_LIST_TAG),
            ) {
                items(
                    count = podcasts.itemCount,
                    key = podcasts.itemKey { it.id },
                    contentType = podcasts.itemContentType { "podcast" },
                ) { index ->
                    val podcast = podcasts[index] ?: return@items
                    PodcastListItem(
                        podcast = podcast,
                        isFavorite = podcast.id in favoriteIds,
                        onClick = { onPodcastClick(podcast) },
                        onToggleFavorite = { onToggleFavorite(podcast) },
                        onShare = podcast.listennotesUrl?.let { { onShare(podcast) } },
                    )
                }
                appendFooter(podcasts)
            }
        }
    }
}
