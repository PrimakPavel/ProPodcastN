package com.propodcast.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DTOs for Listen Notes API v2. Only the fields the app uses are declared;
// unknown keys are ignored by the Json configuration.

@Serializable
data class NetworkGenre(
    val id: Int,
    val name: String,
    @SerialName("parent_id") val parentId: Int? = null,
)

@Serializable
data class GenresResponse(val genres: List<NetworkGenre> = emptyList())

/** `regions` is a map of region code → display name, e.g. "us" → "United States". */
@Serializable
data class RegionsResponse(val regions: Map<String, String> = emptyMap())

@Serializable
data class LanguagesResponse(val languages: List<String> = emptyList())

@Serializable
data class NetworkPodcast(
    val id: String,
    val title: String = "",
    val publisher: String = "",
    val image: String? = null,
    val thumbnail: String? = null,
    val description: String = "",
    val language: String? = null,
    val country: String? = null,
    val website: String? = null,
    @SerialName("listennotes_url") val listennotesUrl: String? = null,
    @SerialName("total_episodes") val totalEpisodes: Int = 0,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("latest_pub_date_ms") val latestPubDateMs: Long? = null,
    @SerialName("explicit_content") val explicitContent: Boolean = false,
)

@Serializable
data class BestPodcastsResponse(
    val podcasts: List<NetworkPodcast> = emptyList(),
    @SerialName("has_next") val hasNext: Boolean = false,
    @SerialName("next_page_number") val nextPageNumber: Int? = null,
    @SerialName("page_number") val pageNumber: Int = 1,
    val total: Int = 0,
)

@Serializable
data class NetworkEpisode(
    val id: String,
    val title: String = "",
    val description: String = "",
    val audio: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    @SerialName("pub_date_ms") val pubDateMs: Long = 0,
    @SerialName("audio_length_sec") val audioLengthSec: Int = 0,
    @SerialName("listennotes_url") val listennotesUrl: String? = null,
    @SerialName("explicit_content") val explicitContent: Boolean = false,
)

/** `GET podcasts/{id}`: podcast fields plus one page of episodes. */
@Serializable
data class PodcastDetailsResponse(
    val id: String,
    val title: String = "",
    val publisher: String = "",
    val image: String? = null,
    val thumbnail: String? = null,
    val description: String = "",
    val language: String? = null,
    val country: String? = null,
    val website: String? = null,
    @SerialName("listennotes_url") val listennotesUrl: String? = null,
    @SerialName("total_episodes") val totalEpisodes: Int = 0,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("latest_pub_date_ms") val latestPubDateMs: Long? = null,
    @SerialName("explicit_content") val explicitContent: Boolean = false,
    val episodes: List<NetworkEpisode> = emptyList(),
    @SerialName("next_episode_pub_date") val nextEpisodePubDate: Long? = null,
)

@Serializable
data class RecommendationsResponse(val recommendations: List<NetworkPodcast> = emptyList())

/** One item of `GET search?type=podcast`. Search uses `*_original` field names. */
@Serializable
data class NetworkSearchPodcast(
    val id: String,
    @SerialName("title_original") val title: String = "",
    @SerialName("publisher_original") val publisher: String = "",
    @SerialName("description_original") val description: String = "",
    val image: String? = null,
    val thumbnail: String? = null,
    @SerialName("listennotes_url") val listennotesUrl: String? = null,
    @SerialName("total_episodes") val totalEpisodes: Int = 0,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("latest_pub_date_ms") val latestPubDateMs: Long? = null,
    @SerialName("explicit_content") val explicitContent: Boolean = false,
)

@Serializable
data class SearchPodcastsResponse(
    val results: List<NetworkSearchPodcast> = emptyList(),
    val count: Int = 0,
    val total: Int = 0,
    @SerialName("next_offset") val nextOffset: Int? = null,
)
