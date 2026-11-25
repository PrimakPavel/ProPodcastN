package com.propodcast.app.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.propodcast.app.R
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.shareLink
import com.propodcast.app.feature.filters.FiltersSheet
import com.propodcast.app.feature.filters.FiltersTab
import com.propodcast.app.feature.filters.FiltersViewModel

@Composable
fun HomeScreen(
    onPodcastClick: (Podcast) -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    filtersViewModel: FiltersViewModel = hiltViewModel(),
) {
    val podcasts = viewModel.podcasts.collectAsLazyPagingItems()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val quota by viewModel.quota.collectAsStateWithLifecycle()
    val hasDownloads by viewModel.hasDownloads.collectAsStateWithLifecycle()
    val filtersState by filtersViewModel.uiState.collectAsStateWithLifecycle()
    var sheetTab by rememberSaveable { mutableStateOf<FiltersTab?>(null) }
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.share_chooser_title)

    HomeContent(
        podcasts = podcasts,
        favoriteIds = favoriteIds,
        quota = quota,
        filtersState = filtersState,
        onPodcastClick = onPodcastClick,
        onToggleFavorite = viewModel::toggleFavorite,
        onShare = { podcast ->
            podcast.listennotesUrl?.let { context.shareLink(podcast.title, it, shareTitle) }
        },
        onOpenFilters = { tab -> sheetTab = tab },
        onOpenDownloads = onOpenDownloads.takeIf { hasDownloads },
        onOpenSettings = onOpenSettings,
        onClearGenre = { filtersViewModel.selectGenre(null) },
        onClearRegion = { filtersViewModel.selectRegion(null) },
        modifier = modifier,
    )

    sheetTab?.let { tab ->
        FiltersSheet(
            state = filtersState,
            onSelectGenre = filtersViewModel::selectGenre,
            onSelectRegion = filtersViewModel::selectRegion,
            onRetryGenres = filtersViewModel::loadGenres,
            onRetryRegions = filtersViewModel::loadRegions,
            onDismiss = { sheetTab = null },
            initialTab = tab,
        )
    }
}
