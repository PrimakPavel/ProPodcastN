package com.propodcast.app.feature.home

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.model.PodcastFilters
import com.propodcast.app.core.testing.FakeApiQuotaRepository
import com.propodcast.app.core.testing.FakeFavoritesRepository
import com.propodcast.app.core.testing.FakePodcastRepository
import com.propodcast.app.core.testing.FakeUserPreferencesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import com.propodcast.app.core.testing.testPodcast
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val podcastRepository = FakePodcastRepository().apply {
        bestPodcastsResult = { genreId, region -> listOf(testPodcast("$genreId-$region")) }
    }
    private val preferences = FakeUserPreferencesRepository(PodcastFilters(genreId = 93, regionCode = "ua"))
    private val favorites = FakeFavoritesRepository()
    private val quota = FakeApiQuotaRepository()

    private fun viewModel() = HomeViewModel(podcastRepository, preferences, quota, favorites)

    @Test
    fun podcasts_useSavedGenreAndRegion() = runTest {
        val items = viewModel().podcasts.asSnapshot()

        assertEquals(listOf("93-ua"), items.map { it.id })
        assertEquals(listOf(93 to "ua"), podcastRepository.bestPodcastRequests)
    }

    @Test
    fun podcasts_reloadWhenFilterChanges_butNotForLanguage() = runTest {
        val viewModel = viewModel()
        viewModel.podcasts.asSnapshot()

        preferences.setLanguage("English") // search-only filter: no reload
        preferences.setGenre(null)
        val items = viewModel.podcasts.asSnapshot()

        assertEquals(listOf("null-ua"), items.map { it.id })
        assertEquals(listOf(93 to "ua", null to "ua"), podcastRepository.bestPodcastRequests)
    }

    @Test
    fun toggleFavorite_addsThenRemoves() = runTest {
        val viewModel = viewModel()
        val podcast = testPodcast("p1")

        viewModel.favoriteIds.test {
            assertEquals(emptySet<String>(), awaitItem())

            viewModel.toggleFavorite(podcast)
            assertEquals(setOf("p1"), awaitItem())

            viewModel.toggleFavorite(podcast)
            assertEquals(emptySet<String>(), awaitItem())
        }
    }

    @Test
    fun quota_isExposedFromRepository() = runTest {
        val viewModel = viewModel()
        val value = ApiQuota(used = 1, freeQuota = 10, nextBillingDate = null, plan = "FREE")

        quota.quota.value = value

        assertEquals(value, viewModel.quota.value)
    }
}
