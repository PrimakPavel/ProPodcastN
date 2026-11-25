package com.propodcast.app.core.di

import android.content.Context
import androidx.room.Room
import com.propodcast.app.core.database.ProPodcastDatabase
import com.propodcast.app.core.database.dao.DownloadedEpisodeDao
import com.propodcast.app.core.database.dao.FavoriteEpisodeDao
import com.propodcast.app.core.database.dao.FavoritePodcastDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ProPodcastDatabase =
        Room.databaseBuilder(context, ProPodcastDatabase::class.java, ProPodcastDatabase.NAME).build()

    @Provides
    fun provideFavoritePodcastDao(db: ProPodcastDatabase): FavoritePodcastDao = db.favoritePodcastDao()

    @Provides
    fun provideFavoriteEpisodeDao(db: ProPodcastDatabase): FavoriteEpisodeDao = db.favoriteEpisodeDao()

    @Provides
    fun provideDownloadedEpisodeDao(db: ProPodcastDatabase): DownloadedEpisodeDao = db.downloadedEpisodeDao()
}
