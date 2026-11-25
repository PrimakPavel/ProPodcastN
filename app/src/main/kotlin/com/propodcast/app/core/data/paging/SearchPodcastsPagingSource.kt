package com.propodcast.app.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.mapper.toModel

/** `search` is paged by offset, using `next_offset` and `total`. Only forward paging is needed. */
class SearchPodcastsPagingSource(
    private val api: ListenNotesApi,
    private val query: String,
    private val language: String?,
) : PagingSource<Int, Podcast>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Podcast> {
        val offset = params.key ?: 0
        return apiCall { api.searchPodcasts(query = query, offset = offset, language = language) }
            .fold(
                onSuccess = { response ->
                    val next = response.nextOffset
                    val hasMore = response.results.isNotEmpty() && next != null && next > offset && next < response.total
                    LoadResult.Page(
                        data = response.results.map { it.toModel() },
                        prevKey = null,
                        nextKey = if (hasMore) next else null,
                    )
                },
                onFailure = { LoadResult.Error(it) },
            )
    }

    // Restart from the beginning on refresh: offsets are not stable across requests.
    override fun getRefreshKey(state: PagingState<Int, Podcast>): Int? = null
}
