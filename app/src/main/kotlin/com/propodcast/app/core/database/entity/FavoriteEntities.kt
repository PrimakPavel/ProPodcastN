package com.propodcast.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast

@Entity(tableName = "favorite_podcasts")
data class FavoritePodcastEntity(
    @PrimaryKey val id: String,
    val title: String,
    val publisher: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    val description: String,
    @ColumnInfo(name = "total_episodes") val totalEpisodes: Int,
    @ColumnInfo(name = "listennotes_url") val listennotesUrl: String?,
    @ColumnInfo(name = "latest_pub_date_ms") val latestPubDateMs: Long?,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

@Entity(tableName = "favorite_episodes")
data class FavoriteEpisodeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "audio_url") val audioUrl: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    @ColumnInfo(name = "pub_date_ms") val pubDateMs: Long,
    @ColumnInfo(name = "duration_sec") val durationSec: Int,
    @ColumnInfo(name = "listennotes_url") val listennotesUrl: String?,
    @ColumnInfo(name = "podcast_id") val podcastId: String?,
    @ColumnInfo(name = "podcast_title") val podcastTitle: String?,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

fun FavoritePodcastEntity.toModel() = Podcast(
    id = id,
    title = title,
    publisher = publisher,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    description = description,
    totalEpisodes = totalEpisodes,
    listennotesUrl = listennotesUrl,
    latestPubDateMs = latestPubDateMs,
)

fun Podcast.toFavoriteEntity(addedAt: Long) = FavoritePodcastEntity(
    id = id,
    title = title,
    publisher = publisher,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    description = description,
    totalEpisodes = totalEpisodes,
    listennotesUrl = listennotesUrl,
    latestPubDateMs = latestPubDateMs,
    addedAt = addedAt,
)

fun FavoriteEpisodeEntity.toModel() = Episode(
    id = id,
    title = title,
    description = description,
    audioUrl = audioUrl,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    pubDateMs = pubDateMs,
    durationSec = durationSec,
    listennotesUrl = listennotesUrl,
    podcastId = podcastId,
    podcastTitle = podcastTitle,
)

fun Episode.toFavoriteEntity(addedAt: Long) = FavoriteEpisodeEntity(
    id = id,
    title = title,
    description = description,
    audioUrl = audioUrl,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    pubDateMs = pubDateMs,
    durationSec = durationSec,
    listennotesUrl = listennotesUrl,
    podcastId = podcastId,
    podcastTitle = podcastTitle,
    addedAt = addedAt,
)
