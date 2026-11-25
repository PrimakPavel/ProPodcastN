package com.propodcast.app.core.model

/** A podcast as shown in lists, details and favorites. */
data class Podcast(
    val id: String,
    val title: String,
    val publisher: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val description: String,
    val totalEpisodes: Int,
    val listennotesUrl: String?,
    val latestPubDateMs: Long? = null,
    val language: String? = null,
    val country: String? = null,
    val website: String? = null,
    val genreIds: List<Int> = emptyList(),
    val explicitContent: Boolean = false,
)

/** A playable episode. [podcastId] / [podcastTitle] identify its podcast (used by favorites and the player). */
data class Episode(
    val id: String,
    val title: String,
    val description: String,
    val audioUrl: String?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val pubDateMs: Long,
    val durationSec: Int,
    val listennotesUrl: String?,
    val podcastId: String?,
    val podcastTitle: String?,
    val explicitContent: Boolean = false,
)

/** One page of podcast details. Load more with [nextEpisodePubDate] while [hasMoreEpisodes]. */
data class PodcastDetails(
    val podcast: Podcast,
    val episodes: List<Episode>,
    val nextEpisodePubDate: Long?,
) {
    val hasMoreEpisodes: Boolean
        get() = nextEpisodePubDate != null && episodes.isNotEmpty()
}

data class Genre(val id: Int, val name: String, val parentId: Int?)

data class Region(val code: String, val name: String)

/** Filters for best podcasts (genre, region) and search (language). Null means "any". */
data class PodcastFilters(
    val genreId: Int? = null,
    val regionCode: String? = null,
    val language: String? = null,
)

/** The last episode the user played, used to resume playback and by the widget. */
data class LastPlayed(
    val episodeId: String,
    val title: String,
    val podcastTitle: String?,
    val audioUrl: String,
    val imageUrl: String?,
    val positionMs: Long,
)
