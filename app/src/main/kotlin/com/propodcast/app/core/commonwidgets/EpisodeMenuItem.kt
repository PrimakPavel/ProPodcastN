package com.propodcast.app.core.commonwidgets

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.propodcast.app.R
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.core.ui.EpisodeDownloadActions

/** One entry of an episode row's ⋮ menu. */
internal class EpisodeMenuItem(
    @param:StringRes val labelRes: Int,
    val icon: @Composable () -> Painter,
    val onClick: () -> Unit,
)

/** Share (when possible) and the download action for the current state. */
internal fun episodeMenuItems(
    episode: Episode,
    state: EpisodeDownloadState?,
    downloads: EpisodeDownloadActions,
    onShare: (() -> Unit)?,
): List<EpisodeMenuItem> = buildList {
    if (onShare != null) add(EpisodeMenuItem(R.string.action_share, { rememberVectorPainter(Icons.Filled.Share) }, onShare))
    val download = downloads.onDownload?.let { action ->
        EpisodeMenuItem(R.string.action_download, { painterResource(R.drawable.ic_download) }) { action(episode) }
    }
    val delete = downloads.onDelete?.let { action ->
        EpisodeMenuItem(R.string.action_delete_download, { rememberVectorPainter(Icons.Filled.Delete) }) { action(episode) }
    }
    when (state?.status) {
        null -> if (episode.audioUrl != null) addIfNotNull(download)
        DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING -> downloads.onCancel?.let { action ->
            add(EpisodeMenuItem(R.string.action_cancel_download, { rememberVectorPainter(Icons.Filled.Close) }) { action(episode) })
        }
        DownloadStatus.DOWNLOADED -> addIfNotNull(delete)
        DownloadStatus.FAILED -> {
            addIfNotNull(download)
            addIfNotNull(delete)
        }
    }
}

private fun <T> MutableList<T>.addIfNotNull(item: T?) {
    if (item != null) add(item)
}
