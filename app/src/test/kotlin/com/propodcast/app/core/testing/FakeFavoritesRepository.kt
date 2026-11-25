package com.propodcast.app.core.testing

import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavoritesRepository : FavoritesRepository {
    private val podcasts = MutableStateFlow<List<Podcast>>(emptyList())
    private val episodes = MutableStateFlow<List<Episode>>(emptyList())

    override val favoritePodcasts: Flow<List<Podcast>> = podcasts
    override val favoriteEpisodes: Flow<List<Episode>> = episodes
    override val favoritePodcastIds: Flow<Set<String>> = podcasts.map { list -> list.map { it.id }.toSet() }
    override val favoriteEpisodeIds: Flow<Set<String>> = episodes.map { list -> list.map { it.id }.toSet() }

    override suspend fun setPodcastFavorite(podcast: Podcast, favorite: Boolean) {
        podcasts.value = podcasts.value.filterNot { it.id == podcast.id } + listOfNotNull(podcast.takeIf { favorite })
    }

    override suspend fun setEpisodeFavorite(episode: Episode, favorite: Boolean) {
        episodes.value = episodes.value.filterNot { it.id == episode.id } + listOfNotNull(episode.takeIf { favorite })
    }
}
