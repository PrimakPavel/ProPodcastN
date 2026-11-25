package com.propodcast.app.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.propodcast.app.core.database.ProPodcastDatabase
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
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

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ProPodcastDatabase::class.java).build()
        repository = DefaultFavoritesRepository(db.favoritePodcastDao(), db.favoriteEpisodeDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun podcasts_addNewestFirst_andRemove() = runTest {
        repository.setPodcastFavorite(podcast("p1"), favorite = true)
        Thread.sleep(2) // distinct added_at
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
}
