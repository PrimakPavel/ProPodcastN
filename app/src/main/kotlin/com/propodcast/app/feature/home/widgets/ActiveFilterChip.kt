package com.propodcast.app.feature.home.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.propodcast.app.R

/** One active filter: tap opens the filters sheet, the close icon clears it. */
@Composable
internal fun ActiveFilterChip(label: String, onClick: () -> Unit, onClear: () -> Unit) {
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
