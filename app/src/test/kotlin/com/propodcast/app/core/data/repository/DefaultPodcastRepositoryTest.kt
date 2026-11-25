package com.propodcast.app.core.data.repository

import com.propodcast.app.core.testing.FakeAnalyticsTracker
import com.propodcast.app.core.testing.ListenNotesTestServer
import com.propodcast.app.core.testing.TestClock
import com.propodcast.app.core.testing.testDataStore
import com.propodcast.app.core.testing.testReferenceDataCache
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DefaultPodcastRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val server = ListenNotesTestServer()
    private val clock = TestClock(nowMs = 1_000)
    private val analytics = FakeAnalyticsTracker()

    private fun TestScope.repository() =
        DefaultPodcastRepository(server.api, testReferenceDataCache(testDataStore(tmp.root, backgroundScope), clock), analytics)

    @After
    fun tearDown() = server.close()

    @Test
    fun podcastDetails_mapsEpisodesWithPodcastInfo() = runTest(UnconfinedTestDispatcher()) {
        server.enqueueFixture("podcast")

        val details = repository().podcastDetails("ee84d7d11875465fb89487675ff5425d").getOrThrow()

        assertEquals("Star Wars 7x7: A Daily Bite-Sized Dose of Star Wars Joy", details.podcast.title)
        assertTrue(details.hasMoreEpisodes)
        details.episodes.forEach { episode ->
            assertEquals(details.podcast.id, episode.podcastId)
            assertEquals(details.podcast.title, episode.podcastTitle)
            assertTrue(episode.audioUrl!!.startsWith("https://"))
        }
    }

    @Test
    fun podcastDetails_withoutNextPubDate_hasNoMoreEpisodes() = runTest(UnconfinedTestDispatcher()) {
        server.enqueue(200, """{"id":"x","title":"T","episodes":[{"id":"e1"}]}""")

        val details = repository().podcastDetails("x").getOrThrow()

        assertFalse(details.hasMoreEpisodes)
    }

    @Test
    fun languages_dropAnyLanguageEntry() = runTest(UnconfinedTestDispatcher()) {
        server.enqueueFixture("languages")

        val languages = repository().languages().getOrThrow()

        assertFalse("Any language" in languages)
        assertEquals(2, languages.size)
    }

    @Test
    fun regions_sortedByName() = runTest(UnconfinedTestDispatcher()) {
        server.enqueueFixture("regions")

        val names = repository().regions().getOrThrow().map { it.name }

        assertEquals(names.sorted(), names)
    }

    @Test
    fun genresRegionsLanguages_areCached_untilAWeekPasses() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()
        server.enqueueFixture("genres")
        server.enqueueFixture("regions")
        server.enqueueFixture("languages")

        val genres = repository.genres().getOrThrow()
        val regions = repository.regions().getOrThrow()
        val languages = repository.languages().getOrThrow()
        assertEquals(3, server.server.requestCount)

        clock.nowMs += TimeUnit.DAYS.toMillis(6)
        assertEquals("cached models round-trip unchanged", genres, repository.genres().getOrThrow())
        assertEquals(regions, repository.regions().getOrThrow())
        assertEquals(languages, repository.languages().getOrThrow())
        assertEquals(3, server.server.requestCount)

        clock.nowMs += TimeUnit.DAYS.toMillis(1)
        server.enqueueFixture("genres")
        repository.genres().getOrThrow()
        assertEquals(4, server.server.requestCount)
    }

    @Test
    fun search_isLogged_withoutTheQuery() = runTest(UnconfinedTestDispatcher()) {
        repository().searchPodcasts("my secret query", "Ukranian")
        repository().searchPodcasts("other", null)

        assertEquals(listOf("search {language_filter=true}", "search {language_filter=false}"), analytics.events)
        assertFalse(analytics.events.any { "secret" in it })
    }

    @Test
    fun failure_isResultFailure() = runTest(UnconfinedTestDispatcher()) {
        server.enqueue(404)

        assertTrue(repository().recommendations("missing").isFailure)
    }
}
