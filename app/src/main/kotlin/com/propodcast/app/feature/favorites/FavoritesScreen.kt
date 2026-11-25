package com.propodcast.app.feature.favorites

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.propodcast.app.R
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.RemovedFavorite
import com.propodcast.app.core.ui.shareLink
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    onPodcastClick: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sharePodcastTitle = stringResource(R.string.share_chooser_title)
    val shareEpisodeTitle = stringResource(R.string.share_episode_chooser_title)
    val playbackSoon = stringResource(R.string.playback_coming_soon)
    val undoLabel = stringResource(R.string.action_undo)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.removed.collect { removed ->
            // A newer removal replaces the previous snackbar instead of waiting in the queue.
            snackbarHostState.currentSnackbarData?.dismiss()
            launch {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.favorite_removed, removed.title()),
                    actionLabel = undoLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.undoRemove(removed)
            }
        }
    }

    FavoritesContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onPodcastClick = onPodcastClick,
        onRemovePodcast = viewModel::removePodcast,
        onSharePodcast = { podcast ->
            podcast.listennotesUrl?.let { context.shareLink(podcast.title, it, sharePodcastTitle) }
        },
        // Playback arrives in stage 6.
        onEpisodeClick = { scope.launch { snackbarHostState.showSnackbar(playbackSoon) } },
        onRemoveEpisode = viewModel::removeEpisode,
        onShareEpisode = { episode ->
            episode.listennotesUrl?.let { context.shareLink(episode.title, it, shareEpisodeTitle) }
        },
        modifier = modifier,
    )
}

private fun RemovedFavorite.title(): String = when (this) {
    is RemovedFavorite.PodcastItem -> podcast.title
    is RemovedFavorite.EpisodeItem -> episode.title
}
