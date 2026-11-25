package com.propodcast.app.feature.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.commonwidgets.OptionsList
import com.propodcast.app.feature.filters.widgets.genreOptions
import com.propodcast.app.feature.filters.widgets.regionOptions

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
