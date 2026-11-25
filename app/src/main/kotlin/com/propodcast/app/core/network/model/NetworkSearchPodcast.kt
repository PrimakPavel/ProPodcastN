package com.propodcast.app.core.network.model

import com.propodcast.app.core.model.Podcast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

fun NetworkSearchPodcast.toModel(): Podcast {
    val image = image.nullIfBlank()
    val thumbnail = thumbnail.nullIfBlank()
    return Podcast(
        id = id,
        title = title,
        publisher = publisher,
        imageUrl = image ?: thumbnail,
        thumbnailUrl = thumbnail ?: image,
        description = description,
        totalEpisodes = totalEpisodes,
        listennotesUrl = listennotesUrl.nullIfBlank(),
        latestPubDateMs = latestPubDateMs,
        genreIds = genreIds,
        explicitContent = explicitContent,
    )
}
