package com.propodcast.app.feature.favorites

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.propodcast.app.R
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.SnackbarEffect
import com.propodcast.app.core.ui.shareLink
import com.propodcast.app.feature.downloads.DownloadsViewModel
import com.propodcast.app.feature.downloads.CellularDownloadPrompt
import com.propodcast.app.feature.downloads.rememberEpisodeDownloadActions
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    onPodcastClick: (Podcast) -> Unit,
    onPlayEpisode: (Episode) -> Boolean,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
    downloadsViewModel: DownloadsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloads = rememberEpisodeDownloadActions(downloadsViewModel)
    val context = LocalContext.current
    val sharePodcastTitle = stringResource(R.string.share_chooser_title)
    val shareEpisodeTitle = stringResource(R.string.share_episode_chooser_title)
    val noAudio = stringResource(R.string.player_no_audio)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    SnackbarEffect(viewModel.snackbarEvents, snackbarHostState)
    SnackbarEffect(downloadsViewModel.snackbarEvents, snackbarHostState)
    CellularDownloadPrompt(downloadsViewModel)

    FavoritesContent(
        state = state,
        downloads = downloads,
        snackbarHostState = snackbarHostState,
        onPodcastClick = onPodcastClick,
        onRemovePodcast = viewModel::removePodcast,
        onSharePodcast = { podcast ->
            podcast.listennotesUrl?.let { context.shareLink(podcast.title, it, sharePodcastTitle) }
        },
        onEpisodeClick = { episode ->
            if (!onPlayEpisode(episode)) scope.launch { snackbarHostState.showSnackbar(noAudio) }
        },
        onRemoveEpisode = viewModel::removeEpisode,
        onShareEpisode = { episode ->
            episode.listennotesUrl?.let { context.shareLink(episode.title, it, shareEpisodeTitle) }
        },
        modifier = modifier,
    )
}

