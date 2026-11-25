package com.propodcast.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data class PodcastDetailsRoute(val podcastId: String) {
    companion object {
        /** SavedStateHandle key of [podcastId] (type-safe routes use the property name). */
        const val ARG_PODCAST_ID = "podcastId"
    }
}
