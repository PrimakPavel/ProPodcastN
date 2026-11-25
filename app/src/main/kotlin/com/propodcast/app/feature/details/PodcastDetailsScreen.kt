package com.propodcast.app.feature.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.propodcast.app.R
import com.propodcast.app.core.designsystem.component.BackButton
import com.propodcast.app.core.designsystem.component.EpisodeListItem
import com.propodcast.app.core.designsystem.component.ListFooterError
import com.propodcast.app.core.designsystem.component.LoadingState
import com.propodcast.app.core.designsystem.component.MessageState
import com.propodcast.app.core.designsystem.component.PodcastCard
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.message
import com.propodcast.app.core.ui.shareLink
import kotlinx.coroutines.launch

@Composable
fun PodcastDetailsScreen(
    onBack: () -> Unit,
    onPodcastClick: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PodcastDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sharePodcastTitle = stringResource(R.string.share_chooser_title)
    val shareEpisodeTitle = stringResource(R.string.share_episode_chooser_title)
    val playbackSoon = stringResource(R.string.playback_coming_soon)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    PodcastDetailsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::load,
        onLoadMore = viewModel::loadMoreEpisodes,
        onTogglePodcastFavorite = viewModel::togglePodcastFavorite,
        onSharePodcast = { podcast ->
            podcast.listennotesUrl?.let { context.shareLink(podcast.title, it, sharePodcastTitle) }
        },
        // Playback arrives in stage 6.
        onEpisodeClick = { scope.launch { snackbarHostState.showSnackbar(playbackSoon) } },
        onToggleEpisodeFavorite = viewModel::toggleEpisodeFavorite,
        onShareEpisode = { episode ->
            episode.listennotesUrl?.let { context.shareLink(episode.title, it, shareEpisodeTitle) }
        },
        onPodcastClick = onPodcastClick,
        modifier = modifier,
    )
}

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

private fun LazyListScope.recommendationsSection(
    recommendations: List<Podcast>,
    onPodcastClick: (Podcast) -> Unit,
) {
    if (recommendations.isEmpty()) return
    item(key = "recommendations-title") { SectionTitle(stringResource(R.string.details_recommendations)) }
    item(key = "recommendations") {
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
            items(recommendations, key = { it.id }) { podcast ->
                PodcastCard(podcast = podcast, onClick = { onPodcastClick(podcast) })
            }
        }
    }
}

private fun LazyListScope.episodesSection(
    content: DetailsContent.Loaded,
    favoriteEpisodeIds: Set<String>,
    onEpisodeClick: (Episode) -> Unit,
    onToggleFavorite: (Episode) -> Unit,
    onShare: (Episode) -> Unit,
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
        )
    }
}

private fun LazyListScope.loadMoreFooter(content: DetailsContent.Loaded, onLoadMore: () -> Unit) {
    when {
        content.isLoadingMore -> item(key = "load-more") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        content.loadMoreError != null -> item(key = "load-more") {
            ListFooterError(message = content.loadMoreError.message(), onRetry = onLoadMore)
        }
        content.canLoadMore -> item(key = "load-more") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                OutlinedButton(onClick = onLoadMore) { Text(stringResource(R.string.details_load_more)) }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

const val DETAILS_LIST_TAG = "details:list"
