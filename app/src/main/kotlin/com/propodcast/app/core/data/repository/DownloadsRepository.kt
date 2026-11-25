package com.propodcast.app.core.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.propodcast.app.core.database.dao.DownloadedEpisodeDao
import com.propodcast.app.core.database.entity.toDownload
import com.propodcast.app.core.database.entity.toDownloadEntity
import com.propodcast.app.core.download.EpisodeDownloadWorker
import com.propodcast.app.core.download.EpisodeFileStore
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.EpisodeDownloadState
import java.io.File
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Episodes downloaded for offline listening. */
interface DownloadsRepository {
    /** Newest first; live progress for running downloads. */
    val downloads: Flow<List<EpisodeDownload>>

    /** episodeId → state, for the indicators in episode lists. */
    val states: Flow<Map<String, EpisodeDownloadState>>

    /** Queues the download (again, if it failed). Episodes without audio are ignored. */
    suspend fun enqueue(episode: Episode)

    /** Stops a queued or running download and forgets it. */
    suspend fun cancel(episodeId: String)

    /** Hides a download for an Undo window; [commitRemove] deletes the file, [undoRemove] brings it back. */
    suspend fun markRemoved(episodeId: String)
    suspend fun undoRemove(episodeId: String)
    suspend fun commitRemove(episodeId: String)

    /** The downloaded file to play instead of streaming, if there is one. */
    suspend fun localFile(episodeId: String): File?
}

@Singleton
class WorkManagerDownloadsRepository @Inject constructor(
    private val dao: DownloadedEpisodeDao,
    private val workManager: WorkManager,
    private val files: EpisodeFileStore,
    private val clock: Clock,
) : DownloadsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Removals whose Undo window was cut short by the process dying.
        scope.launch {
            dao.pendingDeleteIds().forEach { deleteNow(it) }
        }
    }

    /** Progress of running downloads, by episode. */
    private val progress: Flow<Map<String, Float>> =
        workManager.getWorkInfosByTagFlow(EpisodeDownloadWorker.TAG).map { infos ->
            infos.filter { it.state == WorkInfo.State.RUNNING }
                .mapNotNull { info ->
                    val episodeId = EpisodeDownloadWorker.episodeIdFromTags(info.tags) ?: return@mapNotNull null
                    val value = info.progress.getFloat(EpisodeDownloadWorker.KEY_PROGRESS, -1f)
                    episodeId to value
                }
                .filter { (_, value) -> value >= 0f }
                .toMap()
        }

    override val downloads: Flow<List<EpisodeDownload>> =
        combine(dao.observeVisible(), progress) { rows, running ->
            rows.map { it.toDownload(running[it.episodeId]) }
        }.distinctUntilChanged()

    override val states: Flow<Map<String, EpisodeDownloadState>> =
        downloads.map { list -> list.associate { it.episode.id to it.state } }.distinctUntilChanged()

    override suspend fun enqueue(episode: Episode) {
        val entity = episode.toDownloadEntity(addedAt = clock.millis()) ?: return
        dao.upsert(entity)
        workManager.enqueueUniqueWork(
            EpisodeDownloadWorker.uniqueWorkName(episode.id),
            ExistingWorkPolicy.REPLACE,
            EpisodeDownloadWorker.request(episode.id),
        )
    }

    override suspend fun cancel(episodeId: String) {
        workManager.cancelUniqueWork(EpisodeDownloadWorker.uniqueWorkName(episodeId))
        deleteNow(episodeId)
    }

    override suspend fun markRemoved(episodeId: String) = dao.setPendingDelete(episodeId, true)

    override suspend fun undoRemove(episodeId: String) = dao.setPendingDelete(episodeId, false)

    override suspend fun commitRemove(episodeId: String) {
        if (dao.get(episodeId)?.pendingDelete == true) deleteNow(episodeId)
    }

    override suspend fun localFile(episodeId: String): File? {
        val row = dao.get(episodeId) ?: return null
        if (row.status != DownloadStatus.DOWNLOADED || row.pendingDelete) return null
        return files.file(episodeId).takeIf { it.exists() }
    }

    private suspend fun deleteNow(episodeId: String) {
        files.delete(episodeId)
        dao.delete(episodeId)
    }
}
