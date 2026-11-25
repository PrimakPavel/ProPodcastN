package com.propodcast.app.core.di

import com.propodcast.app.core.data.repository.DataStoreUserPreferencesRepository
import com.propodcast.app.core.data.repository.DefaultFavoritesRepository
import com.propodcast.app.core.data.repository.DefaultPodcastRepository
import com.propodcast.app.core.data.repository.FavoritesRepository
import com.propodcast.app.core.data.repository.PodcastRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindPodcastRepository(impl: DefaultPodcastRepository): PodcastRepository

    @Binds
    abstract fun bindFavoritesRepository(impl: DefaultFavoritesRepository): FavoritesRepository

    @Binds
    abstract fun bindUserPreferencesRepository(impl: DataStoreUserPreferencesRepository): UserPreferencesRepository
}
