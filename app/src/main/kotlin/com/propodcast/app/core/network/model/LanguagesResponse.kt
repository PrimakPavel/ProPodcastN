package com.propodcast.app.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LanguagesResponse(val languages: List<String> = emptyList())
