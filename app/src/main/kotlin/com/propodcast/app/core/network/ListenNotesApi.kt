package com.propodcast.app.core.network

import com.propodcast.app.core.network.dto.NetworkPodcast
import com.propodcast.app.core.network.dto.response.BestPodcastsResponse
import com.propodcast.app.core.network.dto.response.GenresResponse
import com.propodcast.app.core.network.dto.response.LanguagesResponse
import com.propodcast.app.core.network.dto.response.RecommendationsResponse
import com.propodcast.app.core.network.dto.response.RegionsResponse
import com.propodcast.app.core.network.dto.response.SearchPodcastsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Listen Notes API v2. The API key header is added by [ApiKeyInterceptor]. Null query params are omitted. */
interface ListenNotesApi {

    @GET("genres")
    suspend fun genres(@Query("top_level_only") topLevelOnly: Int? = null): GenresResponse

    @GET("regions")
    suspend fun regions(): RegionsResponse

    @GET("languages")
    suspend fun languages(): LanguagesResponse

    @GET("best_podcasts")
    suspend fun bestPodcasts(
        @Query("genre_id") genreId: Int? = null,
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null,
    ): BestPodcastsResponse

    @GET("podcasts/{id}")
    suspend fun podcast(
        @Path("id") id: String,
        @Query("next_episode_pub_date") nextEpisodePubDate: Long? = null,
        @Query("sort") sort: String = SORT_RECENT_FIRST,
    ): NetworkPodcast

    @GET("podcasts/{id}/recommendations")
    suspend fun recommendations(@Path("id") id: String): RecommendationsResponse

    @GET("search")
    suspend fun searchPodcasts(
        @Query("q") query: String,
        @Query("offset") offset: Int = 0,
        @Query("language") language: String? = null,
        @Query("type") type: String = TYPE_PODCAST,
    ): SearchPodcastsResponse

    companion object {
        const val SORT_RECENT_FIRST = "recent_first"
        const val TYPE_PODCAST = "podcast"

        /** The "no language filter" entry returned by `GET languages`. */
        const val ANY_LANGUAGE = "Any language"
    }
}
