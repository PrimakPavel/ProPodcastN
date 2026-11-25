package com.propodcast.app.core.player

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.propodcast.app.core.model.NowPlaying

/**
 * The audio URI also goes into requestMetadata: MediaItem.localConfiguration does not survive the trip from
 * MediaController to MediaSession, so [PlaybackService] rebuilds it from there.
 */
internal fun NowPlaying.toMediaItem(): MediaItem {
    val uri = audioUrl.toUri()
    return MediaItem.Builder()
        .setMediaId(episodeId)
        .setUri(uri)
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(podcastTitle)
                .setArtworkUri(imageUrl?.toUri())
                .setDurationMs(durationMs.takeIf { it > 0 })
                .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST_EPISODE)
                .build(),
        )
        .build()
}

internal fun MediaItem.toNowPlaying(): NowPlaying? {
    val uri: Uri = localConfiguration?.uri ?: requestMetadata.mediaUri ?: return null
    return NowPlaying(
        episodeId = mediaId,
        title = mediaMetadata.title?.toString().orEmpty(),
        podcastTitle = mediaMetadata.artist?.toString(),
        audioUrl = uri.toString(),
        imageUrl = mediaMetadata.artworkUri?.toString(),
        durationMs = mediaMetadata.durationMs ?: 0,
    )
}

/** Restores the playable URI on the session side (see [toMediaItem]). */
internal fun MediaItem.withPlayableUri(): MediaItem =
    if (localConfiguration != null) this else buildUpon().setUri(requestMetadata.mediaUri).build()
