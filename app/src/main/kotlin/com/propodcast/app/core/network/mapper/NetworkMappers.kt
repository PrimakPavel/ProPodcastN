package com.propodcast.app.core.network.mapper

import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.PodcastDetails
import com.propodcast.app.core.model.Region
import com.propodcast.app.core.network.model.NetworkEpisode
import com.propodcast.app.core.network.model.NetworkGenre
import com.propodcast.app.core.network.model.NetworkPodcast
import com.propodcast.app.core.network.model.NetworkSearchPodcast
import com.propodcast.app.core.network.model.PodcastDetailsResponse
import com.propodcast.app.core.network.model.RegionsResponse

/** The API's "no language filter" entry from `GET languages`. */
const val ANY_LANGUAGE = "Any language"

fun NetworkPodcast.toModel() = Podcast(
    id = id,
    title = title,
    publisher = publisher,
    imageUrl = image,
    thumbnailUrl = thumbnail,
    description = description,
    totalEpisodes = totalEpisodes,
    listennotesUrl = listennotesUrl,
    latestPubDateMs = latestPubDateMs,
    language = language,
    country = country,
    website = website,
    genreIds = genreIds,
    explicitContent = explicitContent,
)

fun NetworkSearchPodcast.toModel() = Podcast(
    id = id,
    title = title,
    publisher = publisher,
    imageUrl = image,
    thumbnailUrl = thumbnail,
    description = description,
    totalEpisodes = totalEpisodes,
    listennotesUrl = listennotesUrl,
    latestPubDateMs = latestPubDateMs,
    genreIds = genreIds,
    explicitContent = explicitContent,
)

fun NetworkEpisode.toModel(podcastId: String?, podcastTitle: String?) = Episode(
    id = id,
    title = title,
    description = description,
    audioUrl = audio,
    imageUrl = image,
    thumbnailUrl = thumbnail,
    pubDateMs = pubDateMs,
    durationSec = audioLengthSec,
    listennotesUrl = listennotesUrl,
    podcastId = podcastId,
    podcastTitle = podcastTitle,
    explicitContent = explicitContent,
)

fun PodcastDetailsResponse.toModel() = PodcastDetails(
    podcast = Podcast(
        id = id,
        title = title,
        publisher = publisher,
        imageUrl = image,
        thumbnailUrl = thumbnail,
        description = description,
        totalEpisodes = totalEpisodes,
        listennotesUrl = listennotesUrl,
        latestPubDateMs = latestPubDateMs,
        language = language,
        country = country,
        website = website,
        genreIds = genreIds,
        explicitContent = explicitContent,
    ),
    episodes = episodes.map { it.toModel(podcastId = id, podcastTitle = title) },
    nextEpisodePubDate = nextEpisodePubDate,
)

fun NetworkGenre.toModel() = Genre(id = id, name = name, parentId = parentId)

fun RegionsResponse.toModels(): List<Region> =
    regions.map { (code, name) -> Region(code = code, name = name) }.sortedBy { it.name }
