package com.propodcast.app.core.data.repository

import com.propodcast.app.core.database.dao.FavoriteEpisodeDao
import com.propodcast.app.core.database.dao.FavoritePodcastDao
import com.propodcast.app.core.database.entity.toFavoriteEntity
import com.propodcast.app.core.database.entity.toModel
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Favorite podcasts and episodes stored locally, newest first. */
interface FavoritesRepository {
    val favoritePodcasts: Flow<List<Podcast>>
    val favoriteEpisodes: Flow<List<Episode>>

    /** Ids for marking list items as favorite without loading full rows. */
    val favoritePodcastIds: Flow<Set<String>>
    val favoriteEpisodeIds: Flow<Set<String>>

    suspend fun setPodcastFavorite(podcast: Podcast, favorite: Boolean)
    suspend fun setEpisodeFavorite(episode: Episode, favorite: Boolean)
}

@Singleton
class DefaultFavoritesRepository @Inject constructor(
    private val podcastDao: FavoritePodcastDao,
    private val episodeDao: FavoriteEpisodeDao,
    private val clock: Clock,
) : FavoritesRepository {

    override val favoritePodcasts: Flow<List<Podcast>> =
        podcastDao.observeAll().map { rows -> rows.map { it.toModel() } }

    override val favoriteEpisodes: Flow<List<Episode>> =
        episodeDao.observeAll().map { rows -> rows.map { it.toModel() } }

    override val favoritePodcastIds: Flow<Set<String>> = podcastDao.observeIds().map { it.toSet() }

    override val favoriteEpisodeIds: Flow<Set<String>> = episodeDao.observeIds().map { it.toSet() }

    override suspend fun setPodcastFavorite(podcast: Podcast, favorite: Boolean) {
        if (favorite) {
            podcastDao.upsert(podcast.toFavoriteEntity(addedAt = clock.millis()))
        } else {
            podcastDao.delete(podcast.id)
        }
    }

    override suspend fun setEpisodeFavorite(episode: Episode, favorite: Boolean) {
        if (favorite) {
            episodeDao.upsert(episode.toFavoriteEntity(addedAt = clock.millis()))
        } else {
            episodeDao.delete(episode.id)
        }
    }
}
