package com.propodcast.app.feature.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FavoritesContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun podcast(id: String) = Podcast(
        id = id,
        title = "Podcast $id",
        publisher = "Publisher",
        imageUrl = null,
        thumbnailUrl = null,
        description = "",
        totalEpisodes = 5,
        listennotesUrl = "https://www.listennotes.com/c/$id/",
    )

    private fun episode(id: String) = Episode(
        id = id,
        title = "Episode $id",
        description = "",
        audioUrl = null,
        imageUrl = null,
        thumbnailUrl = null,
        pubDateMs = 0,
        durationSec = 60,
        listennotesUrl = null,
        podcastId = "p1",
        podcastTitle = "Podcast p1",
    )

    private val clicked = mutableListOf<String>()

    private fun setContent(state: FavoritesUiState) {
        composeRule.setContent {
            ProPodcastTheme {
                FavoritesContent(
                    state = state,
                    onPodcastClick = { clicked += "open ${it.id}" },
                    onRemovePodcast = { clicked += "remove ${it.id}" },
                    onSharePodcast = { clicked += "share ${it.id}" },
                    onEpisodeClick = { clicked += "play ${it.id}" },
                    onRemoveEpisode = { clicked += "remove ${it.id}" },
                    onShareEpisode = { clicked += "share ${it.id}" },
                )
            }
        }
    }

    @Test
    fun emptyTabs_showHints() {
        setContent(FavoritesUiState(isLoading = false))

        composeRule.onNodeWithText("No favorite podcasts yet.", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Episodes").performClick()
        composeRule.onNodeWithText("No favorite episodes yet.", substring = true).assertIsDisplayed()
    }

    @Test
    fun tabsShowCounts_andSwitchPages() {
        setContent(FavoritesUiState(listOf(podcast("p1"), podcast("p2")), listOf(episode("e1")), isLoading = false))

        composeRule.onNodeWithText("Podcasts (2)").assertIsDisplayed()
        composeRule.onNodeWithText("Podcast p1").performClick()
        composeRule.onNodeWithText("Episodes (1)").performClick()
        composeRule.onNodeWithText("Episode e1").performClick()

        assertEquals(listOf("open p1", "play e1"), clicked)
    }

    @Test
    fun heartButton_removes() {
        setContent(FavoritesUiState(listOf(podcast("p1")), emptyList(), isLoading = false))

        composeRule.onAllNodesWithContentDescription("Remove from favorites")[0].performClick()

        assertEquals(listOf("remove p1"), clicked)
    }

    @Test
    fun swipeEitherWay_removes() {
        setContent(FavoritesUiState(listOf(podcast("p1"), podcast("p2")), emptyList(), isLoading = false))

        composeRule.onNodeWithText("Podcast p1").performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("Podcast p2").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        assertEquals(listOf("remove p1", "remove p2"), clicked)
    }

    @Test
    fun rowRestoredByUndo_canBeSwipedAgain() {
        val p1 = podcast("p1")
        var podcasts by mutableStateOf(listOf(p1))
        composeRule.setContent {
            ProPodcastTheme {
                FavoritesContent(
                    state = FavoritesUiState(podcasts, emptyList(), isLoading = false),
                    onPodcastClick = {},
                    onRemovePodcast = { removed ->
                        clicked += "remove ${removed.id}"
                        podcasts = podcasts - removed
                    },
                    onSharePodcast = {},
                    onEpisodeClick = {},
                    onRemoveEpisode = {},
                    onShareEpisode = {},
                )
            }
        }

        composeRule.onNodeWithText("Podcast p1").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        podcasts = listOf(p1) // Undo
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Podcast p1").assertIsDisplayed().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        assertEquals(listOf("remove p1", "remove p1"), clicked)
    }

    @Test
    fun loading_hidesLists() {
        setContent(FavoritesUiState())
        composeRule.onNodeWithTag(FAVORITE_PODCASTS_LIST_TAG).assertDoesNotExist()
    }
}
