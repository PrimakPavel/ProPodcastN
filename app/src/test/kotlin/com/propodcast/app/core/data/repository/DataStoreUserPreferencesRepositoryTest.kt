package com.propodcast.app.core.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.propodcast.app.core.model.LastPlayed
import com.propodcast.app.core.model.PodcastFilters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreUserPreferencesRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.repository() = DataStoreUserPreferencesRepository(
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tmp.newFile("prefs.preferences_pb").also { it.delete() } },
        ),
    )

    @Test
    fun filters_defaultToAny_andPersistChanges() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()
        repository.filters.test {
            assertEquals(PodcastFilters(), awaitItem())

            repository.setGenre(93)
            assertEquals(PodcastFilters(genreId = 93), awaitItem())

            repository.setRegion("ua")
            repository.setLanguage("Ukrainian")
            assertEquals(PodcastFilters(93, "ua", null), awaitItem())
            assertEquals(PodcastFilters(93, "ua", "Ukrainian"), awaitItem())

            repository.setGenre(null)
            assertEquals(PodcastFilters(null, "ua", "Ukrainian"), awaitItem())
        }
    }

    @Test
    fun lastPlayed_saveUpdatePositionAndClear() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()
        val track = LastPlayed(
            episodeId = "e1",
            title = "Episode",
            podcastTitle = null,
            audioUrl = "https://example.com/a.mp3",
            imageUrl = "https://example.com/a.jpg",
            positionMs = 0,
            durationMs = 3_600_000,
        )
        repository.lastPlayed.test {
            assertNull(awaitItem())

            repository.setLastPlayed(track)
            assertEquals(track, awaitItem())

            repository.updateLastPlayedPosition(42_000)
            assertEquals(track.copy(positionMs = 42_000), awaitItem())

            repository.clearLastPlayed()
            assertNull(awaitItem())
        }
    }

    @Test
    fun updatePosition_withoutTrack_isIgnored() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()
        repository.updateLastPlayedPosition(1_000)
        repository.lastPlayed.test {
            assertNull(awaitItem())
        }
    }
}
