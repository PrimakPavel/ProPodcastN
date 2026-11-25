package com.propodcast.app.core.network

import com.propodcast.app.core.testing.ListenNotesTestServer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListenNotesApiTest {

    private val server = ListenNotesTestServer(apiKey = "test-key")

    @After
    fun tearDown() = server.close()

    @Test
    fun bestPodcasts_parsesPageAndOmitsNullFilters() = runTest {
        server.enqueueFixture("best_podcasts")

        val response = server.api.bestPodcasts(page = 1)

        assertEquals(2, response.podcasts.size)
        assertTrue(response.hasNext)
        assertEquals(2, response.nextPageNumber)
        assertEquals("THE ED MYLETT SHOW", response.podcasts.first().title)

        val request = server.server.takeRequest()
        assertEquals("/api/v2/best_podcasts", request.url.encodedPath)
        assertEquals("1", request.url.queryParameter("page"))
        assertNull(request.url.queryParameter("genre_id"))
        assertNull(request.url.queryParameter("region"))
        assertEquals("test-key", request.headers[ApiKeyInterceptor.HEADER])
    }

    @Test
    fun podcast_parsesEpisodesAndNextPubDate() = runTest {
        server.enqueueFixture("podcast")

        val response = server.api.podcast(id = "abc", nextEpisodePubDate = 123L)

        assertEquals(2, response.episodes.size)
        assertEquals(1478329202571, response.nextEpisodePubDate)
        val request = server.server.takeRequest()
        assertEquals("/api/v2/podcasts/abc", request.url.encodedPath)
        assertEquals("123", request.url.queryParameter("next_episode_pub_date"))
        assertEquals("recent_first", request.url.queryParameter("sort"))
    }

    @Test
    fun search_usesPodcastTypeAndOriginalFieldNames() = runTest {
        server.enqueueFixture("search")

        val response = server.api.searchPodcasts(query = "star", offset = 10, language = "English")

        val result = response.results.single()
        assertEquals("THE ED MYLETT SHOW", result.title)
        assertTrue(result.publisher.isNotEmpty())
        assertEquals(10, response.nextOffset)
        val url = server.server.takeRequest().url
        assertEquals("podcast", url.queryParameter("type"))
        assertEquals("10", url.queryParameter("offset"))
        assertEquals("English", url.queryParameter("language"))
    }

    @Test
    fun referenceData_parses() = runTest {
        server.enqueueFixture("genres")
        server.enqueueFixture("regions")
        server.enqueueFixture("languages")
        server.enqueueFixture("recommendations")

        assertEquals(3, server.api.genres().genres.size)
        assertEquals("Albania", server.api.regions().regions["al"])
        assertEquals("Any language", server.api.languages().languages.first())
        assertEquals(1, server.api.recommendations("abc").recommendations.size)
    }

    @Test
    fun noApiKey_headerNotSent() = runTest {
        ListenNotesTestServer(apiKey = "").use { noKey ->
            noKey.enqueueFixture("languages")
            noKey.api.languages()
            assertNull(noKey.server.takeRequest().headers[ApiKeyInterceptor.HEADER])
        }
    }
}
