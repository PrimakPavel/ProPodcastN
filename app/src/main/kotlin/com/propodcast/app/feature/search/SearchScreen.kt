package com.propodcast.app.feature.search

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
import com.propodcast.app.feature.search.widgets.LanguageSheet

@Composable
fun SearchScreen(
    onPodcastClick: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val languageFilter by viewModel.languageFilter.collectAsStateWithLifecycle()
    val results = viewModel.results.collectAsLazyPagingItems()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    var showLanguages by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.share_chooser_title)

    SearchContent(
        query = query,
        language = languageFilter.selected,
        results = results,
        favoriteIds = favoriteIds,
        onQueryChange = viewModel::onQueryChange,
        onOpenLanguages = { showLanguages = true },
        onPodcastClick = onPodcastClick,
        onToggleFavorite = viewModel::toggleFavorite,
        onShare = { podcast -> podcast.listennotesUrl?.let { context.shareLink(podcast.title, it, shareTitle) } },
        modifier = modifier,
    )

    if (showLanguages) {
        LanguageSheet(
            state = languageFilter,
            onSelect = { language ->
                viewModel.selectLanguage(language)
                showLanguages = false
            },
            onRetry = viewModel::loadLanguages,
            onDismiss = { showLanguages = false },
        )
    }
}
