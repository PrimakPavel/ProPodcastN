package com.propodcast.app.core.testing

import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.LastPlayed
import com.propodcast.app.core.model.PodcastFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeUserPreferencesRepository(initial: PodcastFilters = PodcastFilters()) : UserPreferencesRepository {
    override val filters: MutableStateFlow<PodcastFilters> = MutableStateFlow(initial)
    override val lastPlayed: StateFlow<LastPlayed?> = MutableStateFlow(null)

    override suspend fun setGenre(genreId: Int?) {
        filters.value = filters.value.copy(genreId = genreId)
    }

    override suspend fun setRegion(regionCode: String?) {
        filters.value = filters.value.copy(regionCode = regionCode)
    }

    override suspend fun setLanguage(language: String?) {
        filters.value = filters.value.copy(language = language)
    }

    override suspend fun setLastPlayed(lastPlayed: LastPlayed) = Unit
    override suspend fun updateLastPlayedPosition(positionMs: Long) = Unit
    override suspend fun clearLastPlayed() = Unit
}
