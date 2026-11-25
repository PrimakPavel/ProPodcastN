package com.propodcast.app.feature.favorites.widgets

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.EpisodeListItem
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.model.Episode
import com.propodcast.app.feature.favorites.FAVORITE_EPISODES_LIST_TAG

@Composable
internal fun FavoriteEpisodesList(
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
    onRemove: (Episode) -> Unit,
    onShare: (Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (episodes.isEmpty()) {
        MessageState(stringResource(R.string.favorites_episodes_empty), modifier)
        return
    }
    LazyColumn(modifier.testTag(FAVORITE_EPISODES_LIST_TAG)) {
        items(episodes, key = { it.id }) { episode ->
            SwipeToRemove(onRemove = { onRemove(episode) }, modifier = removableItemModifier()) {
                EpisodeListItem(
                    episode = episode,
                    isFavorite = true,
                    onClick = { onEpisodeClick(episode) },
                    onToggleFavorite = { onRemove(episode) },
                    onShare = episode.listennotesUrl?.let { { onShare(episode) } },
                )
            }
        }
    }
}
