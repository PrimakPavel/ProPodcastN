package com.propodcast.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.propodcast.app.core.database.dao.FavoriteEpisodeDao
import com.propodcast.app.core.database.dao.FavoritePodcastDao
import com.propodcast.app.core.database.entity.FavoriteEpisodeEntity
import com.propodcast.app.core.database.entity.FavoritePodcastEntity

@Database(
    entities = [FavoritePodcastEntity::class, FavoriteEpisodeEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ProPodcastDatabase : RoomDatabase() {
    abstract fun favoritePodcastDao(): FavoritePodcastDao
    abstract fun favoriteEpisodeDao(): FavoriteEpisodeDao

    companion object {
        const val NAME = "propodcast.db"
    }
}
