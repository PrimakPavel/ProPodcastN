package com.propodcast.app.feature.favorites

import app.cash.turbine.test
import com.propodcast.app.core.model.RemovedFavorite
import com.propodcast.app.core.testing.FakeFavoritesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import com.propodcast.app.core.testing.testEpisode
import com.propodcast.app.core.testing.testPodcast
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val favorites = FakeFavoritesRepository()

    /** uiState is WhileSubscribed: keep a collector so combine() runs. */
    private fun TestScope.subscribed() = FavoritesViewModel(favorites).also { vm ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
    }

    @Test
    fun loading_untilRoomEmits_thenNewestFirst() = runTest {
        assertTrue(FavoritesViewModel(favorites).uiState.value.isLoading)

        favorites.setPodcastFavorite(testPodcast("p1"), true)
        favorites.setPodcastFavorite(testPodcast("p2"), true)
        favorites.setEpisodeFavorite(testEpisode("e1"), true)
        val vm = subscribed()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("p2", "p1"), state.podcasts.map { it.id })
        assertEquals(listOf("e1"), state.episodes.map { it.id })
    }

    @Test
    fun removePodcast_emitsRemoved_andUndoRestoresSamePosition() = runTest {
        listOf("p1", "p2", "p3").forEach { favorites.setPodcastFavorite(testPodcast(it), true) }
        val vm = subscribed()

        vm.removed.test {
            vm.removePodcast(testPodcast("p2"))
            val removed = awaitItem()
            assertEquals("p2", (removed as RemovedFavorite.PodcastItem).podcast.id)
            assertEquals(listOf("p3", "p1"), vm.uiState.value.podcasts.map { it.id })

            vm.undoRemove(removed)
            assertEquals("undo keeps the original order", listOf("p3", "p2", "p1"), vm.uiState.value.podcasts.map { it.id })
        }
    }

    @Test
    fun removeEpisode_emitsRemoved() = runTest {
        favorites.setEpisodeFavorite(testEpisode("e1"), true)
        val vm = subscribed()

        vm.removed.test {
            vm.removeEpisode(testEpisode("e1"))
            assertEquals("e1", (awaitItem() as RemovedFavorite.EpisodeItem).episode.id)
            assertTrue(vm.uiState.value.episodes.isEmpty())
        }
    }

    @Test
    fun removingMissingFavorite_emitsNothing() = runTest {
        val vm = subscribed()
        vm.removed.test {
            vm.removePodcast(testPodcast("nope"))
            expectNoEvents()
        }
    }
}
