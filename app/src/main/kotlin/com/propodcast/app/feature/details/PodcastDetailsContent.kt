package com.propodcast.app.feature.details

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.BackButton
import com.propodcast.app.core.commonwidgets.LoadingState
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.message
import com.propodcast.app.feature.details.widgets.episodesSection
import com.propodcast.app.feature.details.widgets.PodcastDetailsHeader
import com.propodcast.app.feature.details.widgets.loadMoreFooter
import com.propodcast.app.feature.details.widgets.recommendationsSection

/** Stateless details content, also used by UI tests. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetailsContent(
    state: PodcastDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onTogglePodcastFavorite: () -> Unit,
    onSharePodcast: (Podcast) -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onToggleEpisodeFavorite: (Episode) -> Unit,
    onShareEpisode: (Episode) -> Unit,
    onPodcastClick: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val loaded = state.content as? DetailsContent.Loaded
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = loaded?.podcast?.title ?: stringResource(R.string.podcast_details_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    if (loaded != null) {
                        IconButton(onClick = onTogglePodcastFavorite) {
                            Icon(
                                imageVector = if (state.isPodcastFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = stringResource(
                                    if (state.isPodcastFavorite) {
                                        R.string.action_remove_from_favorites
                                    } else {
                                        R.string.action_add_to_favorites
                                    },
                                ),
                            )
                        }
                        if (loaded.podcast.listennotesUrl != null) {
                            IconButton(onClick = { onSharePodcast(loaded.podcast) }) {
                                Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share))
                            }
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (val content = state.content) {
            DetailsContent.Loading -> LoadingState(contentModifier)
            is DetailsContent.Error -> MessageState(content.error.message(), contentModifier, onRetry = onRetry)
            is DetailsContent.Loaded -> LazyColumn(
                modifier = contentModifier.testTag(DETAILS_LIST_TAG),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                item(key = "header") { PodcastDetailsHeader(content.podcast) }
                recommendationsSection(state.recommendations, onPodcastClick)
                episodesSection(content, state.favoriteEpisodeIds, onEpisodeClick, onToggleEpisodeFavorite, onShareEpisode)
                loadMoreFooter(content, onLoadMore)
            }
        }
    }
}
