package com.propodcast.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.propodcast.app.R
import com.propodcast.app.core.designsystem.component.FloatingHeader
import com.propodcast.app.core.designsystem.component.ListFooterError
import com.propodcast.app.core.designsystem.component.LoadingState
import com.propodcast.app.core.designsystem.component.MessageState
import com.propodcast.app.core.designsystem.component.PodcastListItem
import com.propodcast.app.core.designsystem.component.QuotaBanner
import com.propodcast.app.core.designsystem.component.rememberFloatingHeaderScrollBehavior
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.shareLink
import com.propodcast.app.core.ui.userMessage
import com.propodcast.app.feature.filters.FiltersSheet
import com.propodcast.app.feature.filters.FiltersTab
import com.propodcast.app.feature.filters.FiltersUiState
import com.propodcast.app.feature.filters.FiltersViewModel

@Composable
fun HomeScreen(
    onPodcastClick: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    filtersViewModel: FiltersViewModel = hiltViewModel(),
) {
    val podcasts = viewModel.podcasts.collectAsLazyPagingItems()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val quota by viewModel.quota.collectAsStateWithLifecycle()
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

/** Stateless home content, also used by UI tests. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    podcasts: LazyPagingItems<Podcast>,
    favoriteIds: Set<String>,
    quota: ApiQuota?,
    filtersState: FiltersUiState,
    onPodcastClick: (Podcast) -> Unit,
    onToggleFavorite: (Podcast) -> Unit,
    onShare: (Podcast) -> Unit,
    onOpenFilters: (FiltersTab) -> Unit,
    onClearGenre: () -> Unit,
    onClearRegion: () -> Unit,
    modifier: Modifier = Modifier,
    filtersPinned: Boolean = false,
) {
    // Filter chips hide while scrolling down and come back on scroll up (pass filtersPinned = true to keep them).
    val filtersScrollBehavior = rememberFloatingHeaderScrollBehavior(pinned = filtersPinned)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.best_podcasts_title)) },
                actions = {
                    IconButton(onClick = { onOpenFilters(FiltersTab.GENRE) }) {
                        BadgedBox(badge = { if (filtersState.hasActiveFilters) Badge() }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_filter_list),
                                contentDescription = stringResource(R.string.action_filters),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(filtersScrollBehavior.nestedScrollConnection),
        ) {
            QuotaBanner(quota)
            FloatingHeader(filtersScrollBehavior, Modifier.testTag(FILTER_CHIPS_TAG)) {
                ActiveFilters(filtersState, onOpenFilters, onClearGenre, onClearRegion)
            }
            PodcastsList(podcasts, favoriteIds, onPodcastClick, onToggleFavorite, onShare)
        }
    }
}

@Composable
private fun ActiveFilters(
    state: FiltersUiState,
    onOpenFilters: (FiltersTab) -> Unit,
    onClearGenre: () -> Unit,
    onClearRegion: () -> Unit,
) {
    val genre = state.selectedGenre
    val region = state.selectedRegion
    if (genre == null && region == null) return
    // Chips are 32dp tall; without the 48dp minimum touch-target padding they sit right under the app bar.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (genre != null) {
                item(key = "genre") {
                    FilterChip(genre.name, onClick = { onOpenFilters(FiltersTab.GENRE) }, onClear = onClearGenre)
                }
            }
            if (region != null) {
                item(key = "region") {
                    FilterChip(region.name, onClick = { onOpenFilters(FiltersTab.REGION) }, onClear = onClearRegion)
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, onClick: () -> Unit, onClear: () -> Unit) {
    InputChip(
        selected = true,
        onClick = onClick,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.action_clear_filter, label),
                modifier = Modifier
                    .size(InputChipDefaults.AvatarSize)
                    .clickable(role = Role.Button, onClick = onClear),
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PodcastsList(
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

private fun LazyGridScope.appendFooter(podcasts: LazyPagingItems<Podcast>) {
    when (val append = podcasts.loadState.append) {
        is LoadState.Loading -> item(key = "append-loading", span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> item(key = "append-error", span = { GridItemSpan(maxLineSpan) }) {
            ListFooterError(message = append.error.userMessage(), onRetry = podcasts::retry)
        }
        is LoadState.NotLoading -> Unit
    }
}

const val PODCASTS_LIST_TAG = "home:podcasts"
const val FILTER_CHIPS_TAG = "home:filterChips"
