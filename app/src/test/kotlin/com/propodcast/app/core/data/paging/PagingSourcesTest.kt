package com.propodcast.app.core.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.AppException
import com.propodcast.app.core.testing.ListenNotesTestServer
import com.propodcast.app.core.testing.fixture
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PagingSourcesTest {

    private val server = ListenNotesTestServer()
    private val config = PagingConfig(pageSize = 20, enablePlaceholders = false)

    @After
    fun tearDown() = server.close()

    @Test
    fun bestPodcasts_firstPage_hasNextKeyFromResponse() = runTest {
        server.enqueueFixture("best_podcasts")
        val pager = TestPager(config, BestPodcastsPagingSource(server.api, genreId = 93, region = "us"))

        val page = pager.refresh() as LoadResult.Page

        assertEquals(2, page.data.size)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
        val url = server.server.takeRequest().url
        assertEquals("93", url.queryParameter("genre_id"))
        assertEquals("us", url.queryParameter("region"))
    }

    @Test
    fun bestPodcasts_lastPage_hasNoNextKey() = runTest {
        server.enqueue(200, fixture("best_podcasts").replace("\"has_next\": true", "\"has_next\": false"))
        val pager = TestPager(config, BestPodcastsPagingSource(server.api, genreId = null, region = null))

        val page = pager.refresh() as LoadResult.Page

        assertNull(page.nextKey)
    }

    @Test
    fun bestPodcasts_httpError_isAppException() = runTest {
        server.enqueue(429)
        val pager = TestPager(config, BestPodcastsPagingSource(server.api, genreId = null, region = null))

        val error = pager.refresh() as LoadResult.Error

        assertEquals(AppError.TooManyRequests, (error.throwable as AppException).error)
    }

    @Test
    fun search_nextKeyIsNextOffset_untilTotalReached() = runTest {
        server.enqueueFixture("search") // total 25, next_offset 10
        val pager = TestPager(config, SearchPodcastsPagingSource(server.api, query = "star", language = null))

        val first = pager.refresh() as LoadResult.Page
        assertEquals(10, first.nextKey)
        assertNull(server.server.takeRequest().url.queryParameter("language"))

        server.enqueue(200, fixture("search").replace("\"next_offset\": 10", "\"next_offset\": 30"))
        val second = pager.append() as LoadResult.Page
        assertTrue(second.data.isNotEmpty())
        assertNull("next_offset beyond total ends paging", second.nextKey)
        assertEquals("10", server.server.takeRequest().url.queryParameter("offset"))
    }
}
