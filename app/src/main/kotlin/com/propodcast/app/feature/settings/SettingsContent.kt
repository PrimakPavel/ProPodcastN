package com.propodcast.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.BackButton
import com.propodcast.app.feature.settings.widgets.SettingSwitchRow
import com.propodcast.app.feature.settings.widgets.SettingsSectionTitle

/** Stateless settings, also used by UI tests. */
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onDownloadOnlyOnWifiChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionTitle(stringResource(R.string.settings_section_downloads))
            SettingSwitchRow(
                title = stringResource(R.string.settings_wifi_only),
                summary = stringResource(R.string.settings_wifi_only_summary),
                checked = state.downloadOnlyOnWifi,
                onCheckedChange = onDownloadOnlyOnWifiChange,
            )
        }
    }
}
