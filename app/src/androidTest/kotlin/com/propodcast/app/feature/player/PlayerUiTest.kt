package com.propodcast.app.feature.player

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.NowPlaying
import com.propodcast.app.core.player.PlayerError
import com.propodcast.app.core.player.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlayerUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val item = NowPlaying(
        episodeId = "e1",
        title = "Episode one",
        podcastTitle = "The Podcast",
        audioUrl = "https://example.com/e1.mp3",
        imageUrl = null,
        durationMs = 3_723_000,
    )
    private val calls = mutableListOf<String>()

    private fun setMiniPlayer(state: PlayerUiState) = composeRule.setContent {
        ProPodcastTheme {
            MiniPlayer(
                state = state,
                onOpen = { calls += "open" },
                onTogglePlayPause = { calls += "toggle" },
                onClose = { calls += "close" },
            )
        }
    }

    private fun setFullPlayer(state: PlayerUiState) = composeRule.setContent {
        ProPodcastTheme {
            FullPlayerContent(
                state = state,
                onCollapse = { calls += "collapse" },
                onTogglePlayPause = { calls += "toggle" },
                onSeekTo = { calls += "seekTo $it" },
                onSeekBack = { calls += "back" },
                onSeekForward = { calls += "forward" },
                onSpeedSelected = { calls += "speed $it" },
            )
        }
    }

    @Test
    fun miniPlayer_hiddenWithoutEpisode() {
        setMiniPlayer(PlayerUiState())
        composeRule.onNodeWithTag(MINI_PLAYER_TAG).assertDoesNotExist()
    }

    @Test
    fun miniPlayer_showsEpisode_andForwardsClicks() {
        setMiniPlayer(PlayerUiState(PlayerState(nowPlaying = item, isPlaying = true)))

        composeRule.onNodeWithText("Episode one").assertIsDisplayed()
        composeRule.onNodeWithText("The Podcast").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Pause").performClick()
        composeRule.onNodeWithContentDescription("Close player").performClick()
        composeRule.onNodeWithText("Episode one").performClick()

        assertEquals(listOf("toggle", "close", "open"), calls)
    }

    @Test
    fun miniPlayer_restoredEpisode_offersToContinue() {
        setMiniPlayer(PlayerUiState(PlayerState(nowPlaying = item, positionMs = 60_000), isRestored = true))

        composeRule.onNodeWithText("Continue listening").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Play").assertIsDisplayed()
    }

    @Test
    fun miniPlayer_showsError() {
        setMiniPlayer(PlayerUiState(PlayerState(nowPlaying = item, error = PlayerError.NETWORK)))

        composeRule.onNodeWithText("Can’t load the episode. Check your connection.").assertIsDisplayed()
    }

    @Test
    fun fullPlayer_showsTimes_andForwardsControls() {
        setFullPlayer(PlayerUiState(PlayerState(nowPlaying = item, positionMs = 61_000, durationMs = 3_723_000, speed = 1.5f)))

        composeRule.onNodeWithText("1:01").assertIsDisplayed()
        composeRule.onNodeWithText("-1:01:02").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Back 15 seconds").performClick()
        composeRule.onNodeWithContentDescription("Play").performClick()
        composeRule.onNodeWithContentDescription("Forward 30 seconds").performClick()
        composeRule.onNodeWithContentDescription("Playback speed 1.5×").performClick()
        composeRule.onNodeWithText("2×").performClick()
        composeRule.onNodeWithContentDescription("Collapse player").performClick()

        assertEquals(listOf("back", "toggle", "forward", "speed 2.0", "collapse"), calls)
    }

    @Test
    fun fullPlayer_unknownDuration_disablesSeekBar() {
        setFullPlayer(PlayerUiState(PlayerState(nowPlaying = item.copy(durationMs = 0))))

        composeRule.onNodeWithTag(PLAYER_SEEK_BAR_TAG).assertIsNotEnabled()
    }
}
