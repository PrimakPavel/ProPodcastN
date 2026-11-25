package com.propodcast.app.widget

import com.propodcast.app.core.model.toLastPlayed
import com.propodcast.app.core.model.toNowPlaying
import com.propodcast.app.core.player.PlayerError
import com.propodcast.app.core.player.PlayerState
import com.propodcast.app.core.testing.testEpisode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerWidgetStateTest {

    private val live = testEpisode("e1").toNowPlaying()!!
    private val saved = testEpisode("e2").toNowPlaying()!!.toLastPlayed(positionMs = 5_000)

    @Test
    fun nothingPlayed_isEmpty() {
        assertNull(playerWidgetState(PlayerState(), lastPlayed = null))
    }

    @Test
    fun livePlayer_winsOverSavedEpisode() {
        val state = playerWidgetState(PlayerState(nowPlaying = live, isPlaying = true), saved)!!

        assertEquals("Episode e1", state.episodeTitle)
        assertEquals("Podcast p1", state.podcastTitle)
        assertTrue(state.isPlaying)
    }

    @Test
    fun savedEpisode_isShownPaused() {
        val state = playerWidgetState(PlayerState(), saved)!!

        assertEquals("Episode e2", state.episodeTitle)
        assertFalse(state.isPlaying)
    }

    @Test
    fun positionChanges_doNotChangeTheState() {
        val first = playerWidgetState(PlayerState(nowPlaying = live, isPlaying = true, positionMs = 1_000), null)
        val later = playerWidgetState(PlayerState(nowPlaying = live, isPlaying = true, positionMs = 9_000), null)

        assertEquals("the widget is not redrawn on progress ticks", first, later)
    }

    @Test
    fun playerError_isFlagged() {
        assertTrue(playerWidgetState(PlayerState(nowPlaying = live, error = PlayerError.SOURCE), null)!!.hasError)
    }
}
