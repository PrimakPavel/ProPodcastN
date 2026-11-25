package com.propodcast.app.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.propodcast.app.core.database.entity.FavoriteEpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteEpisodeDao {
    /** Newest first; rowid breaks ties when two rows share added_at. */
    @Query("SELECT * FROM favorite_episodes ORDER BY added_at DESC, rowid DESC")
    fun observeAll(): Flow<List<FavoriteEpisodeEntity>>

    @Query("SELECT id FROM favorite_episodes")
    fun observeIds(): Flow<List<String>>

    @Upsert
    suspend fun upsert(episode: FavoriteEpisodeEntity)

    @Query("DELETE FROM favorite_episodes WHERE id = :id")
    suspend fun delete(id: String)
}
