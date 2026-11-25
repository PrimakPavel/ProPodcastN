package com.propodcast.app.feature.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.propodcast.app.R
import com.propodcast.app.core.designsystem.component.LoadingState
import com.propodcast.app.core.designsystem.component.MessageState
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.Region
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.ui.message

/** Bottom sheet with Genre / Region tabs. A selection is applied right away; swipe down to close. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(
    state: FiltersUiState,
    onSelectGenre: (Int?) -> Unit,
    onSelectRegion: (String?) -> Unit,
    onRetryGenres: () -> Unit,
    onRetryRegions: () -> Unit,
    onDismiss: () -> Unit,
    initialTab: FiltersTab = FiltersTab.GENRE,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                FiltersTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.ordinal,
                        onClick = { selectedTab = tab.ordinal },
                        text = { Text(stringResource(tab.titleRes)) },
                    )
                }
            }
            val listModifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
            when (FiltersTab.entries[selectedTab]) {
                FiltersTab.GENRE -> OptionsList(state.genres, onRetryGenres, listModifier) { genres ->
                    genreOptions(genres, state.filters.genreId, onSelectGenre)
                }
                FiltersTab.REGION -> OptionsList(state.regions, onRetryRegions, listModifier) { regions ->
                    regionOptions(regions, state.effectiveRegionCode, onSelectRegion)
                }
            }
        }
    }
}

enum class FiltersTab(val titleRes: Int) {
    GENRE(R.string.filters_genre),
    REGION(R.string.filters_region),
}

@Composable
private fun <T> OptionsList(
    options: OptionsState<T>,
    onRetry: () -> Unit,
    modifier: Modifier,
    content: LazyListScope.(List<T>) -> Unit,
) {
    when (options) {
        OptionsState.Loading -> LoadingState(modifier)
        is OptionsState.Error -> MessageState(
            message = stringResource(R.string.filters_load_error) + "\n" + options.error.message(),
            onRetry = onRetry,
            modifier = modifier,
        )
        is OptionsState.Loaded -> LazyColumn(modifier) { content(options.items) }
    }
}

private fun LazyListScope.genreOptions(genres: List<Genre>, selectedId: Int?, onSelect: (Int?) -> Unit) {
    item(key = "all") {
        OptionRow(label = stringResource(R.string.filters_all_genres), selected = selectedId == null) { onSelect(null) }
    }
    items(genres, key = { it.id }) { genre ->
        OptionRow(label = genre.name, selected = genre.id == selectedId) { onSelect(genre.id) }
    }
}

private fun LazyListScope.regionOptions(regions: List<Region>, selectedCode: String, onSelect: (String?) -> Unit) {
    items(regions, key = { it.code }) { region ->
        val label = if (region.code == ListenNotesApi.DEFAULT_REGION) {
            stringResource(R.string.filters_default_region_suffix, region.name)
        } else {
            region.name
        }
        OptionRow(label = label, selected = region.code == selectedCode) { onSelect(region.code) }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}
