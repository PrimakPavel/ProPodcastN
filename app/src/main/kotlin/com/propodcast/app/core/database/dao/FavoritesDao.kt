package com.propodcast.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.propodcast.app.core.database.entity.FavoriteEpisodeEntity
import com.propodcast.app.core.database.entity.FavoritePodcastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePodcastDao {
    @Query("SELECT * FROM favorite_podcasts ORDER BY added_at DESC")
    fun observeAll(): Flow<List<FavoritePodcastEntity>>

    @Query("SELECT id FROM favorite_podcasts")
    fun observeIds(): Flow<List<String>>

    @Upsert
    suspend fun upsert(podcast: FavoritePodcastEntity)

    @Query("DELETE FROM favorite_podcasts WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface FavoriteEpisodeDao {
    @Query("SELECT * FROM favorite_episodes ORDER BY added_at DESC")
    fun observeAll(): Flow<List<FavoriteEpisodeEntity>>

    @Query("SELECT id FROM favorite_episodes")
    fun observeIds(): Flow<List<String>>

    @Upsert
    suspend fun upsert(episode: FavoriteEpisodeEntity)

    @Query("DELETE FROM favorite_episodes WHERE id = :id")
    suspend fun delete(id: String)
}
