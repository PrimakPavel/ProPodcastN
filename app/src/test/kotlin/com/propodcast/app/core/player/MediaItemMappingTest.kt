package com.propodcast.app.core.player

import android.app.Application
import android.net.Uri
import androidx.media3.common.MediaItem
import com.propodcast.app.core.model.NowPlaying
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** How the player tells a downloaded file from the stream, and what it remembers. Robolectric for android.net.Uri. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class MediaItemMappingTest {

    private val item = NowPlaying(
        episodeId = "e1",
        title = "Episode",
        podcastTitle = "Podcast",
        audioUrl = "https://example.com/e1.mp3",
        imageUrl = "https://example.com/e1.jpg",
        durationMs = 60_000,
    )
    private val file = Uri.fromFile(File("/data/user/0/app/files/episodes/e1.mp3"))

    @Test
    fun streamItem_isNotLocal() {
        assertFalse(item.toMediaItem().playsLocalFile())
    }

    @Test
    fun fileItem_isLocal_butStillRemembersTheStreamUrl() {
        val media = item.toMediaItem(file)

        assertTrue(media.playsLocalFile())
        // The saved "continue" episode must not point at a file that may be deleted later.
        assertEquals(item, media.toNowPlaying())
    }

    @Test
    fun sessionSide_withoutLocalConfiguration_stillKnowsItIsAFile() {
        // What the session gets from the controller: no localConfiguration, URI only in requestMetadata.
        val fromController = MediaItem.Builder()
            .setMediaId("e1")
            .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(file).build())
            .build()

        assertTrue(fromController.playsLocalFile())
        assertEquals(file, fromController.withPlayableUri().localConfiguration?.uri)
    }

    @Test
    fun itemWithoutUri_isNotPlayable() {
        assertNull(MediaItem.Builder().setMediaId("e1").build().toNowPlaying())
    }
}
