package com.propodcast.app.feature.search.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.R

/** Shows the search language ("Any language" when none) and opens the language sheet. */
@Composable
internal fun LanguageChip(
    language: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = language != null,
        onClick = onClick,
        label = { Text(language ?: stringResource(R.string.search_language_any)) },
        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        modifier = modifier.padding(horizontal = 16.dp),
    )
}
