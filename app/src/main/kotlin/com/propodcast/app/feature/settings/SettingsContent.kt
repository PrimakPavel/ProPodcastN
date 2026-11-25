package com.propodcast.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.BackButton
import com.propodcast.app.core.model.ThemeMode
import com.propodcast.app.feature.settings.widgets.SettingRadioRow
import com.propodcast.app.feature.settings.widgets.SettingSwitchRow
import com.propodcast.app.feature.settings.widgets.SettingsSectionTitle

/** Stateless settings, also used by UI tests. */
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
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
            SettingsSectionTitle(stringResource(R.string.settings_section_theme))
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    SettingRadioRow(
                        title = stringResource(mode.labelRes),
                        selected = state.themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                    )
                }
            }
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

private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }
