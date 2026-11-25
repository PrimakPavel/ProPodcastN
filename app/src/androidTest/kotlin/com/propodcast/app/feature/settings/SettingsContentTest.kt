package com.propodcast.app.feature.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import org.junit.Rule
import org.junit.Test

class SettingsContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun wifiOnlyRow_togglesTheSwitch() {
        var state by mutableStateOf(SettingsUiState())
        composeRule.setContent {
            ProPodcastTheme {
                SettingsContent(
                    state = state,
                    onBack = {},
                    onThemeModeChange = { state = state.copy(themeMode = it) },
                    onDownloadOnlyOnWifiChange = { state = state.copy(downloadOnlyOnWifi = it) },
                )
            }
        }

        composeRule.onNodeWithText("Download only on Wi-Fi").assertIsOff().performClick()
        composeRule.onNodeWithText("Download only on Wi-Fi").assertIsOn()
    }

    @Test
    fun themeRows_selectOneMode() {
        var state by mutableStateOf(SettingsUiState())
        composeRule.setContent {
            ProPodcastTheme {
                SettingsContent(
                    state = state,
                    onBack = {},
                    onThemeModeChange = { state = state.copy(themeMode = it) },
                    onDownloadOnlyOnWifiChange = {},
                )
            }
        }

        composeRule.onNodeWithText("Same as device").assertIsSelected()
        composeRule.onNodeWithText("Dark").performClick().assertIsSelected()
        composeRule.onNodeWithText("Same as device").assertIsNotSelected()
    }
}
