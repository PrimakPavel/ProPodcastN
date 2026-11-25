package com.propodcast.app.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.propodcast.app.core.database.ProPodcastDatabase
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultFavoritesRepositoryTest {

    private lateinit var db: ProPodcastDatabase
    private lateinit var repository: DefaultFavoritesRepository
    private val clock = TestClock(nowMs = 1_000)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ProPodcastDatabase::class.java).build()
        repository = DefaultFavoritesRepository(db.favoritePodcastDao(), db.favoriteEpisodeDao(), clock)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun podcasts_addNewestFirst_andRemove() = runTest {
        repository.setPodcastFavorite(podcast("p1"), favorite = true)
        clock.nowMs += 1
        repository.setPodcastFavorite(podcast("p2"), favorite = true)

        repository.favoritePodcasts.test {
            assertEquals(listOf("p2", "p1"), awaitItem().map { it.id })

            repository.setPodcastFavorite(podcast("p2"), favorite = false)
            assertEquals(listOf("p1"), awaitItem().map { it.id })
        }
        repository.favoritePodcastIds.test {
            assertEquals(setOf("p1"), awaitItem())
        }
    }

    @Test
    fun addedAt_comesFromClock_andTiesKeepInsertOrder() = runTest {
        // Same millisecond: rowid keeps "last added first".
        repository.setPodcastFavorite(podcast("p1"), favorite = true)
        repository.setPodcastFavorite(podcast("p2"), favorite = true)
        repository.setPodcastFavorite(podcast("p3"), favorite = true)

        repository.favoritePodcasts.test {
            assertEquals(listOf("p3", "p2", "p1"), awaitItem().map { it.id })
        }
        assertEquals(listOf(1_000L), db.query("SELECT DISTINCT added_at FROM favorite_podcasts", null).use { c ->
            buildList { while (c.moveToNext()) add(c.getLong(0)) }
        })
    }

    @Test
    fun remove_thenRestore_keepsOriginalPosition() = runTest {
        repository.setPodcastFavorite(podcast("p1"), favorite = true)
        clock.nowMs += 1
        repository.setPodcastFavorite(podcast("p2"), favorite = true)
        clock.nowMs += 1
        repository.setPodcastFavorite(podcast("p3"), favorite = true)

        val removed = repository.removePodcast("p2")!!
        assertEquals(1_001L, removed.addedAtMs)
        assertEquals(null, repository.removePodcast("p2"))

        clock.nowMs += 10 // restore must not take a new date
        repository.restore(removed)
        repository.favoritePodcasts.test {
            assertEquals(listOf("p3", "p2", "p1"), awaitItem().map { it.id })
        }

        repository.setEpisodeFavorite(episode("e1"), favorite = true)
        val removedEpisode = repository.removeEpisode("e1")!!
        repository.favoriteEpisodes.test {
            assertEquals(emptyList<Episode>(), awaitItem())
            repository.restore(removedEpisode)
            assertEquals(listOf(episode("e1")), awaitItem())
        }
    }

    @Test
    fun refavoriting_movesItemToTop() = runTest {
        repository.setEpisodeFavorite(episode("e1"), favorite = true)
        clock.nowMs += 1
        repository.setEpisodeFavorite(episode("e2"), favorite = true)
        clock.nowMs += 1
        repository.setEpisodeFavorite(episode("e1"), favorite = true)

        repository.favoriteEpisodes.test {
            assertEquals(listOf("e1", "e2"), awaitItem().map { it.id })
        }
    }

    @Test
    fun episodes_roundTripKeepsPodcastInfo() = runTest {
        val episode = episode("e1")
        repository.setEpisodeFavorite(episode, favorite = true)
        repository.setEpisodeFavorite(episode, favorite = true) // upsert, no duplicate

        repository.favoriteEpisodes.test {
            assertEquals(listOf(episode), awaitItem())
        }
        repository.favoriteEpisodeIds.test {
            assertEquals(setOf("e1"), awaitItem())
        }
    }

    private fun podcast(id: String) = Podcast(
        id = id,
        title = "Podcast $id",
        publisher = "Publisher",
        imageUrl = "https://example.com/$id.jpg",
        thumbnailUrl = null,
        description = "",
        totalEpisodes = 10,
        listennotesUrl = null,
        latestPubDateMs = 1L,
    )

    private fun episode(id: String) = Episode(
        id = id,
        title = "Episode $id",
        description = "",
        audioUrl = "https://example.com/$id.mp3",
        imageUrl = null,
        thumbnailUrl = null,
        pubDateMs = 1L,
        durationSec = 60,
        listennotesUrl = null,
        podcastId = "p1",
        podcastTitle = "Podcast p1",
    )

    /** Clock whose time the test sets explicitly. */
    private class TestClock(var nowMs: Long) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(nowMs)
    }
}
