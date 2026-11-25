package com.propodcast.app.feature.search.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.OptionRow
import com.propodcast.app.core.commonwidgets.OptionsList
import com.propodcast.app.feature.search.LanguageFilterState

/** Bottom sheet to pick the search language; picking one applies it and closes the sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguageSheet(
    state: LanguageFilterState,
    onSelect: (String?) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column {
            Text(
                text = stringResource(R.string.search_language_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            OptionsList(
                options = state.languages,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp),
            ) { languages ->
                item(key = "any") {
                    OptionRow(stringResource(R.string.search_language_any), selected = state.selected == null) {
                        onSelect(null)
                    }
                }
                items(languages, key = { it }) { language ->
                    OptionRow(language, selected = language == state.selected) { onSelect(language) }
                }
            }
        }
    }
}
