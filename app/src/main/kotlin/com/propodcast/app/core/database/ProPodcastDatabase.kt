package com.propodcast.app.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.propodcast.app.core.database.dao.DownloadedEpisodeDao
import com.propodcast.app.core.database.dao.FavoriteEpisodeDao
import com.propodcast.app.core.database.dao.FavoritePodcastDao
import com.propodcast.app.core.database.entity.DownloadedEpisodeEntity
import com.propodcast.app.core.database.entity.FavoriteEpisodeEntity
import com.propodcast.app.core.database.entity.FavoritePodcastEntity

@Database(
    entities = [FavoritePodcastEntity::class, FavoriteEpisodeEntity::class, DownloadedEpisodeEntity::class],
    version = 2,
    exportSchema = true,
    // 2: downloaded_episodes (offline playback).
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class ProPodcastDatabase : RoomDatabase() {
    abstract fun favoritePodcastDao(): FavoritePodcastDao
    abstract fun favoriteEpisodeDao(): FavoriteEpisodeDao
    abstract fun downloadedEpisodeDao(): DownloadedEpisodeDao

    companion object {
        const val NAME = "propodcast.db"
    }
}
