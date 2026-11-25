package com.propodcast.app.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.mapper.toModel

/** `best_podcasts` is paged by page number (1-based), using `has_next` / `next_page_number`. */
class BestPodcastsPagingSource(
    private val api: ListenNotesApi,
    private val genreId: Int?,
    private val region: String?,
) : PagingSource<Int, Podcast>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Podcast> {
        val page = params.key ?: FIRST_PAGE
        return apiCall { api.bestPodcasts(genreId = genreId, page = page, region = region) }
            .fold(
                onSuccess = { response ->
                    LoadResult.Page(
                        data = response.podcasts.map { it.toModel() },
                        prevKey = if (page == FIRST_PAGE) null else page - 1,
                        nextKey = if (response.hasNext) response.nextPageNumber ?: (page + 1) else null,
                    )
                },
                onFailure = { LoadResult.Error(it) },
            )
    }

    override fun getRefreshKey(state: PagingState<Int, Podcast>): Int? =
        state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }

    companion object {
        const val FIRST_PAGE = 1
    }
}
