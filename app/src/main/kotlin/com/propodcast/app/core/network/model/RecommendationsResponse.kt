package com.propodcast.app.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationsResponse(val recommendations: List<NetworkPodcast> = emptyList())
