package com.propodcast.app.feature.favorites.widgets

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.commonwidgets.PodcastListItem
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.feature.favorites.FAVORITE_PODCASTS_LIST_TAG

@Composable
internal fun FavoritePodcastsList(
    podcasts: List<Podcast>,
    onPodcastClick: (Podcast) -> Unit,
    onRemove: (Podcast) -> Unit,
    onShare: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (podcasts.isEmpty()) {
        MessageState(stringResource(R.string.favorites_podcasts_empty), modifier)
        return
    }
    LazyColumn(modifier.testTag(FAVORITE_PODCASTS_LIST_TAG)) {
        items(podcasts, key = { it.id }) { podcast ->
            SwipeToRemove(onRemove = { onRemove(podcast) }, modifier = Modifier.animateItem()) {
                PodcastListItem(
                    podcast = podcast,
                    isFavorite = true,
                    onClick = { onPodcastClick(podcast) },
                    onToggleFavorite = { onRemove(podcast) },
                    onShare = podcast.listennotesUrl?.let { { onShare(podcast) } },
                )
            }
        }
    }
}
