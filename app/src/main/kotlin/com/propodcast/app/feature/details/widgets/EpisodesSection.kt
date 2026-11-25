package com.propodcast.app.feature.details.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.EpisodeListItem
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.feature.details.DetailsContent

/** "Episodes" title and the loaded episodes (or a "No episodes yet" note). */
internal fun LazyListScope.episodesSection(
    content: DetailsContent.Loaded,
    favoriteEpisodeIds: Set<String>,
    onEpisodeClick: (Episode) -> Unit,
    onToggleFavorite: (Episode) -> Unit,
    onShare: (Episode) -> Unit,
    downloads: EpisodeDownloadActions,
) {
    item(key = "episodes-title") { SectionTitle(stringResource(R.string.details_episodes)) }
    if (content.episodes.isEmpty()) {
        item(key = "no-episodes") {
            Text(
                text = stringResource(R.string.details_no_episodes),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
    items(content.episodes, key = { "episode-${it.id}" }) { episode ->
        EpisodeListItem(
            episode = episode,
            isFavorite = episode.id in favoriteEpisodeIds,
            onClick = { onEpisodeClick(episode) },
            onToggleFavorite = { onToggleFavorite(episode) },
            onShare = episode.listennotesUrl?.let { { onShare(episode) } },
            downloads = downloads,
        )
    }
}
