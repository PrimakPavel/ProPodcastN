package com.propodcast.app.feature.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import org.junit.Rule
import org.junit.Test

class NetworkStatusBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun followsTheBannerState() {
        var banner by mutableStateOf(NetworkBanner.HIDDEN)
        composeRule.setContent { ProPodcastTheme { NetworkStatusBar(banner) } }

        composeRule.onNodeWithTag(NETWORK_STATUS_BAR_TAG).assertDoesNotExist()

        banner = NetworkBanner.OFFLINE
        composeRule.onNodeWithText("No connection").assertIsDisplayed()

        banner = NetworkBanner.BACK_ONLINE
        composeRule.onNodeWithText("Back online").assertIsDisplayed()

        banner = NetworkBanner.HIDDEN
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(NETWORK_STATUS_BAR_TAG).assertDoesNotExist()
    }
}
