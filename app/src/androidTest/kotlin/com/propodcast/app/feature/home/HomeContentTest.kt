package com.propodcast.app.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.AppException
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.PodcastFilters
import com.propodcast.app.feature.filters.FiltersTab
import com.propodcast.app.feature.filters.FiltersUiState
import com.propodcast.app.feature.filters.OptionsState
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val done = LoadState.NotLoading(endOfPaginationReached = true)

    private fun podcast(id: String, title: String) = Podcast(
        id = id,
        title = title,
        publisher = "Publisher $id",
        imageUrl = null,
        thumbnailUrl = null,
        description = "",
        totalEpisodes = 3,
        listennotesUrl = "https://www.listennotes.com/c/$id/",
    )

    private fun setContent(
        data: PagingData<Podcast>,
        favoriteIds: Set<String> = emptySet(),
        quota: ApiQuota? = null,
        filters: FiltersUiState = FiltersUiState(),
        onToggleFavorite: (Podcast) -> Unit = {},
        onOpenFilters: (FiltersTab) -> Unit = {},
        onClearGenre: () -> Unit = {},
    ) {
        composeRule.setContent {
            ProPodcastTheme {
                HomeContent(
                    podcasts = flowOf(data).collectAsLazyPagingItems(),
                    favoriteIds = favoriteIds,
                    quota = quota,
                    filtersState = filters,
                    onPodcastClick = {},
                    onToggleFavorite = onToggleFavorite,
                    onShare = {},
                    onOpenFilters = onOpenFilters,
                    onClearGenre = onClearGenre,
                    onClearRegion = {},
                )
            }
        }
    }

    @Test
    fun showsPodcasts_withFavoriteState() {
        var toggled: Podcast? = null
        setContent(
            data = PagingData.from(listOf(podcast("1", "First show"), podcast("2", "Second show")), LoadStates(done, done, done)),
            favoriteIds = setOf("1"),
            onToggleFavorite = { toggled = it },
        )

        composeRule.onNodeWithText("First show").assertIsDisplayed()
        composeRule.onNodeWithText("Second show").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Add to favorites").performClick()

        assertEquals("2", toggled?.id)
    }

    @Test
    fun refreshError_showsMessageAndRetry() {
        val error = LoadState.Error(AppException(AppError.TooManyRequests))
        setContent(data = PagingData.from(emptyList(), LoadStates(error, done, done)))

        composeRule.onNodeWithText("Too many requests", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun emptyList_showsEmptyMessage() {
        setContent(data = PagingData.from(emptyList(), LoadStates(done, done, done)))

        composeRule.onNodeWithText("No podcasts for these filters.").assertIsDisplayed()
    }

    @Test
    fun quotaRunningLow_showsBanner() {
        setContent(
            data = PagingData.from(emptyList(), LoadStates(done, done, done)),
            quota = ApiQuota(used = 2100, freeQuota = 2500, nextBillingDate = null, plan = "FREE"),
        )

        composeRule.onNodeWithText("Listen Notes quota is running low: 2100 of 2500 requests used.").assertIsDisplayed()
    }

    @Test
    fun activeGenre_showsChipThatOpensAndClears() {
        var opened: FiltersTab? = null
        var cleared = false
        setContent(
            data = PagingData.from(emptyList(), LoadStates(done, done, done)),
            filters = FiltersUiState(
                genres = OptionsState.Loaded(listOf(Genre(133, "Comedy", 67))),
                filters = PodcastFilters(genreId = 133),
            ),
            onOpenFilters = { opened = it },
            onClearGenre = { cleared = true },
        )

        composeRule.onNodeWithText("Comedy").performClick()
        composeRule.onNodeWithContentDescription("Clear Comedy filter").performClick()

        assertEquals(FiltersTab.GENRE, opened)
        assertEquals(true, cleared)
    }
}
