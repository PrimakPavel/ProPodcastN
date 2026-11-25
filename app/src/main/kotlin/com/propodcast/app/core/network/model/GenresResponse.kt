package com.propodcast.app.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class GenresResponse(val genres: List<NetworkGenre> = emptyList())
