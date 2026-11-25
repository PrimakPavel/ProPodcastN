package com.propodcast.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchPodcastsResponse(
    val results: List<NetworkSearchPodcast> = emptyList(),
    val count: Int = 0,
    val total: Int = 0,
    @SerialName("next_offset") val nextOffset: Int? = null,
)
