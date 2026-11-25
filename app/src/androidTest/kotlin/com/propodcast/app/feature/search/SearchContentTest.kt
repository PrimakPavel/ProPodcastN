package com.propodcast.app.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.Podcast
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val done = LoadState.NotLoading(endOfPaginationReached = true)

    private fun podcast(id: String, title: String) = Podcast(
        id = id,
        title = title,
        publisher = "Publisher",
        imageUrl = null,
        thumbnailUrl = null,
        description = "",
        totalEpisodes = 1,
        listennotesUrl = null,
    )

    /** Real text state, so typing in the field updates the content like the ViewModel would. */
    private fun setContent(
        initialQuery: String = "",
        language: String? = null,
        results: List<Podcast> = emptyList(),
        onOpenLanguages: () -> Unit = {},
    ) {
        composeRule.setContent {
            var query by remember { mutableStateOf(initialQuery) }
            ProPodcastTheme {
                SearchContent(
                    query = query,
                    language = language,
                    results = flowOf(PagingData.from(results, LoadStates(done, done, done))).collectAsLazyPagingItems(),
                    favoriteIds = emptySet(),
                    onQueryChange = { query = it },
                    onOpenLanguages = onOpenLanguages,
                    onPodcastClick = {},
                    onToggleFavorite = {},
                    onShare = {},
                )
            }
        }
    }

    @Test
    fun hintsChangeWithQueryLength() {
        setContent()
        composeRule.onNodeWithText("Find podcasts by name, topic or publisher.").assertIsDisplayed()

        composeRule.onNodeWithTag(SEARCH_FIELD_TAG).performTextInput("st")
        composeRule.onNodeWithText("Type at least 3 characters to search.").assertIsDisplayed()
        composeRule.onNodeWithText("Find podcasts by name, topic or publisher.").assertDoesNotExist()
    }

    @Test
    fun searchableQuery_showsResults_andClearReturnsToStart() {
        setContent(initialQuery = "star", results = listOf(podcast("1", "Star Wars 7x7")))
        composeRule.onNodeWithText("Star Wars 7x7").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Clear search").performClick()

        composeRule.onNodeWithText("Find podcasts by name, topic or publisher.").assertIsDisplayed()
    }

    @Test
    fun noResults_showsEmptyMessageWithQuery() {
        setContent(initialQuery = "zzzz ")

        composeRule.onNodeWithText("Nothing found for “zzzz”.").assertIsDisplayed()
    }

    @Test
    fun languageChip_showsSelectionAndOpensSheet() {
        var opened = false
        setContent(language = "Ukranian", onOpenLanguages = { opened = true })

        composeRule.onNodeWithText("Ukranian").performClick()

        assertTrue(opened)
    }

    @Test
    fun noLanguage_showsAnyLanguage() {
        setContent()

        composeRule.onNodeWithText("Any language").assertIsDisplayed()
    }
}
