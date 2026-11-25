package com.propodcast.app.core.data.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.propodcast.app.core.di.ApiConfig
import com.propodcast.app.core.di.ReferenceCacheDataStore
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Keeps rarely changing API lists (genres, regions, languages) on disk so they don't cost quota on every launch.
 */
interface ReferenceDataCache {
    /**
     * Returns the cached value while it is younger than [maxAgeMs], otherwise calls [fetch] and saves its result.
     * If [fetch] fails, an expired value is still returned: stale lists beat an error.
     */
    suspend fun <T> getOrFetch(
        key: String,
        serializer: KSerializer<T>,
        maxAgeMs: Long = DEFAULT_MAX_AGE_MS,
        fetch: suspend () -> Result<T>,
    ): Result<T>

    companion object {
        val DEFAULT_MAX_AGE_MS: Long = TimeUnit.DAYS.toMillis(7)
    }
}

@Singleton
class DataStoreReferenceDataCache @Inject constructor(
    @param:ReferenceCacheDataStore private val dataStore: DataStore<Preferences>,
    private val json: Json,
    private val clock: Clock,
    apiConfig: ApiConfig,
) : ReferenceDataCache {

    /** Entries from the mock server and the production API must not mix (debug can switch between them). */
    private val source = apiConfig.baseUrl

    override suspend fun <T> getOrFetch(
        key: String,
        serializer: KSerializer<T>,
        maxAgeMs: Long,
        fetch: suspend () -> Result<T>,
    ): Result<T> {
        val entrySerializer = CacheEntry.serializer(serializer)
        val cached = read(key, entrySerializer)
        if (cached != null && clock.millis() - cached.savedAtMs in 0 until maxAgeMs) {
            return Result.success(cached.value)
        }
        return fetch()
            .onSuccess { value -> write(key, entrySerializer, CacheEntry(source, clock.millis(), value)) }
            .recoverCatching { error -> cached?.value ?: throw error }
    }

    private suspend fun <T> read(key: String, serializer: KSerializer<CacheEntry<T>>): CacheEntry<T>? {
        val raw = dataStore.data.first()[stringPreferencesKey(key)] ?: return null
        return try {
            json.decodeFromString(serializer, raw).takeIf { it.source == source }
        } catch (e: SerializationException) {
            // Format changed between app versions: treat as missing, the next fetch overwrites it.
            Timber.w(e, "Dropping unreadable cache entry %s", key)
            null
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Dropping unreadable cache entry %s", key)
            null
        }
    }

    private suspend fun <T> write(key: String, serializer: KSerializer<CacheEntry<T>>, entry: CacheEntry<T>) {
        dataStore.edit { it[stringPreferencesKey(key)] = json.encodeToString(serializer, entry) }
    }
}
