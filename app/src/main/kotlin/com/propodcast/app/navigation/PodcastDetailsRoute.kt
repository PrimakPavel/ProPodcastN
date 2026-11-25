package com.propodcast.app.navigation

import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.podcastArtworkKey
import kotlinx.serialization.Serializable

/**
 * Podcast details. [title] and [artworkUrl] come from the list the user tapped, so the header (and the
 * artwork "hero" flight with [artworkKey]) can show right away while the details load.
 */
@Serializable
data class PodcastDetailsRoute(
    val podcastId: String,
    val title: String? = null,
    val artworkUrl: String? = null,
    val artworkKey: String? = null,
) {
    companion object {
        /** SavedStateHandle key of [podcastId] (type-safe routes use the property name). */
        const val ARG_PODCAST_ID = "podcastId"
    }
}

/** Route to this podcast's details, opened from the [origin] screen. */
fun Podcast.detailsRoute(origin: String) = PodcastDetailsRoute(
    podcastId = id,
    title = title,
    artworkUrl = thumbnailUrl ?: imageUrl,
    artworkKey = podcastArtworkKey(origin, id),
)
