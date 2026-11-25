package com.propodcast.app.feature.filters

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.ui.OptionsState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FiltersSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun closeButton_hidesSheetAndDismisses() {
        var dismissed = 0
        composeRule.setContent {
            ProPodcastTheme {
                FiltersSheet(
                    state = FiltersUiState(genres = OptionsState.Loaded(listOf(Genre(1, "History", null)))),
                    onSelectGenre = {},
                    onSelectRegion = {},
                    onRetryGenres = {},
                    onRetryRegions = {},
                    onDismiss = { dismissed++ },
                )
            }
        }

        composeRule.onNodeWithText("Filters").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close").performClick()
        composeRule.waitForIdle()

        assertEquals(1, dismissed)
    }
}
