package com.propodcast.app.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.data.paging.BestPodcastsPagingSource
import com.propodcast.app.core.data.paging.SearchPodcastsPagingSource
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.model.PodcastDetails
import com.propodcast.app.core.model.Region
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.ListenNotesApi.Companion.ANY_LANGUAGE
import com.propodcast.app.core.network.dto.response.toModels
import com.propodcast.app.core.network.dto.toDetails
import com.propodcast.app.core.network.dto.toModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** Remote podcast data. Failures come back as [Result.failure] with an AppException. */
interface PodcastRepository {
    fun bestPodcasts(genreId: Int?, region: String?): Flow<PagingData<Podcast>>
    fun searchPodcasts(query: String, language: String?): Flow<PagingData<Podcast>>

    /** First page when [nextEpisodePubDate] is null, otherwise the next page of episodes. */
    suspend fun podcastDetails(id: String, nextEpisodePubDate: Long? = null): Result<PodcastDetails>
    suspend fun recommendations(id: String): Result<List<Podcast>>
    suspend fun genres(): Result<List<Genre>>
    suspend fun regions(): Result<List<Region>>

    /** Languages for the search filter, without the API's "Any language" entry. */
    suspend fun languages(): Result<List<String>>
}

@Singleton
class DefaultPodcastRepository @Inject constructor(
    private val api: ListenNotesApi,
) : PodcastRepository {

    override fun bestPodcasts(genreId: Int?, region: String?): Flow<PagingData<Podcast>> =
        Pager(pagingConfig(BestPodcastsPagingSource.PAGE_SIZE)) {
            BestPodcastsPagingSource(api, genreId, region)
        }.flow

    override fun searchPodcasts(query: String, language: String?): Flow<PagingData<Podcast>> =
        Pager(pagingConfig(SearchPodcastsPagingSource.PAGE_SIZE)) {
            SearchPodcastsPagingSource(api, query, language?.takeUnless { it == ANY_LANGUAGE })
        }.flow

    override suspend fun podcastDetails(id: String, nextEpisodePubDate: Long?): Result<PodcastDetails> =
        apiCall { api.podcast(id = id, nextEpisodePubDate = nextEpisodePubDate).toDetails() }

    override suspend fun recommendations(id: String): Result<List<Podcast>> =
        apiCall { api.recommendations(id).recommendations.map { it.toModel() } }

    override suspend fun genres(): Result<List<Genre>> =
        apiCall { api.genres().genres.map { it.toModel() }.sortedBy { it.name } }

    override suspend fun regions(): Result<List<Region>> =
        apiCall { api.regions().toModels().sortedBy { it.name } }

    override suspend fun languages(): Result<List<String>> =
        apiCall { api.languages().languages.filterNot { it == ANY_LANGUAGE } }

    /** Page size matches what the API returns per request; Paging only uses it for prefetching. */
    private fun pagingConfig(pageSize: Int) = PagingConfig(
        pageSize = pageSize,
        initialLoadSize = pageSize,
        prefetchDistance = pageSize / 2,
        enablePlaceholders = false,
    )
}
