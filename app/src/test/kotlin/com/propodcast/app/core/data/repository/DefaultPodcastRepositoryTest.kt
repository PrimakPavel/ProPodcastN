package com.propodcast.app.core.data.repository

import com.propodcast.app.core.testing.ListenNotesTestServer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultPodcastRepositoryTest {

    private val server = ListenNotesTestServer()
    private val repository = DefaultPodcastRepository(server.api)

    @After
    fun tearDown() = server.close()

    @Test
    fun podcastDetails_mapsEpisodesWithPodcastInfo() = runTest {
        server.enqueueFixture("podcast")

        val details = repository.podcastDetails("ee84d7d11875465fb89487675ff5425d").getOrThrow()

        assertEquals("Star Wars 7x7: A Daily Bite-Sized Dose of Star Wars Joy", details.podcast.title)
        assertTrue(details.hasMoreEpisodes)
        details.episodes.forEach { episode ->
            assertEquals(details.podcast.id, episode.podcastId)
            assertEquals(details.podcast.title, episode.podcastTitle)
            assertTrue(episode.audioUrl!!.startsWith("https://"))
        }
    }

    @Test
    fun podcastDetails_withoutNextPubDate_hasNoMoreEpisodes() = runTest {
        server.enqueue(200, """{"id":"x","title":"T","episodes":[{"id":"e1"}]}""")

        val details = repository.podcastDetails("x").getOrThrow()

        assertFalse(details.hasMoreEpisodes)
    }

    @Test
    fun languages_dropAnyLanguageEntry() = runTest {
        server.enqueueFixture("languages")

        val languages = repository.languages().getOrThrow()

        assertFalse("Any language" in languages)
        assertEquals(2, languages.size)
    }

    @Test
    fun regions_sortedByName() = runTest {
        server.enqueueFixture("regions")

        val names = repository.regions().getOrThrow().map { it.name }

        assertEquals(names.sorted(), names)
    }

    @Test
    fun failure_isResultFailure() = runTest {
        server.enqueue(404)

        assertTrue(repository.recommendations("missing").isFailure)
    }
}
