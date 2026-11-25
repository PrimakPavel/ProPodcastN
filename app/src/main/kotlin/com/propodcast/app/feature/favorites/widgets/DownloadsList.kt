package com.propodcast.app.feature.favorites.widgets

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.feature.favorites.DOWNLOADS_LIST_TAG

/**
 * The Downloaded tab: running downloads first, then finished ones. A swipe cancels a running download or
 * deletes a finished / failed one (both with a snackbar to take it back); a tap plays the episode.
 */
@Composable
internal fun DownloadsList(
    downloads: List<EpisodeDownload>,
    actions: EpisodeDownloadActions,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (downloads.isEmpty()) {
        MessageState(stringResource(R.string.favorites_downloads_empty), modifier)
        return
    }
    LazyColumn(modifier.testTag(DOWNLOADS_LIST_TAG)) {
        items(downloads, key = { it.episode.id }) { download ->
            val episode = download.episode
            val cancel = { actions.onCancel?.invoke(episode) }
            SwipeToRemove(
                onRemove = { if (download.state.isActive) cancel() else actions.onDelete?.invoke(episode) },
                modifier = removableItemModifier(),
            ) {
                DownloadListItem(
                    download = download,
                    onClick = { onEpisodeClick(episode) },
                    onCancel = { cancel() },
                    onRetry = { actions.onDownload?.invoke(episode) },
                )
            }
        }
    }
}
