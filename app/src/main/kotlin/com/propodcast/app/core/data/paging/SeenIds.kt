package com.propodcast.app.core.data.paging

import java.util.concurrent.ConcurrentHashMap

/**
 * Remembers item ids already emitted by one PagingSource instance and drops repeats.
 *
 * Listen Notes may return the same podcast on two pages (ranking changes between requests),
 * and the mock server returns the same page for every request. Duplicate ids would crash
 * LazyColumn keys. A new PagingSource (refresh) starts with an empty set.
 * Thread-safe: prepend and append loads may run concurrently.
 */
internal class SeenIds<T>(private val idOf: (T) -> String) {
    private val seen: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /** Items whose id was not seen before, in order; also drops repeats within [items]. */
    fun keepNew(items: List<T>): List<T> = items.filter { seen.add(idOf(it)) }
}
