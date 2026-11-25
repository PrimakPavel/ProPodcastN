package com.propodcast.app.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.RemovedFavorite
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Favorites from Room; removing one emits it on [removed] so the UI can offer Undo. */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        favoritesRepository.favoritePodcasts,
        favoritesRepository.favoriteEpisodes,
    ) { podcasts, episodes ->
        FavoritesUiState(podcasts = podcasts, episodes = episodes, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FavoritesUiState())

    private val removedChannel = Channel<RemovedFavorite>(Channel.BUFFERED)

    /** One event per removal, for the "Removed · Undo" snackbar. */
    val removed: Flow<RemovedFavorite> = removedChannel.receiveAsFlow()

    fun removePodcast(podcast: Podcast) {
        viewModelScope.launch { favoritesRepository.removePodcast(podcast.id)?.let { removedChannel.send(it) } }
    }

    fun removeEpisode(episode: Episode) {
        viewModelScope.launch { favoritesRepository.removeEpisode(episode.id)?.let { removedChannel.send(it) } }
    }

    fun undoRemove(removed: RemovedFavorite) {
        viewModelScope.launch { favoritesRepository.restore(removed) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
