package com.propodcast.app.feature.downloads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.propodcast.app.core.commonwidgets.EpisodeListItem
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.core.model.NowPlaying
import com.propodcast.app.core.player.PlayerState
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.feature.favorites.FavoritesContent
import com.propodcast.app.feature.favorites.FavoritesTab
import com.propodcast.app.feature.favorites.FavoritesUiState
import com.propodcast.app.feature.player.FullPlayerContent
import com.propodcast.app.feature.player.PlayerUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DownloadsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<String>()
    private val actions = EpisodeDownloadActions(
        onDownload = { calls += "download ${it.id}" },
        onCancel = { calls += "cancel ${it.id}" },
        onDelete = { calls += "delete ${it.id}" },
    )

    private fun episode(id: String) = Episode(
        id = id,
        title = "Episode $id",
        description = "",
        audioUrl = "https://example.com/$id.mp3",
        imageUrl = null,
        thumbnailUrl = null,
        pubDateMs = 0,
        durationSec = 60,
        listennotesUrl = null,
        podcastId = "p1",
        podcastTitle = "Podcast p1",
    )

    private fun download(id: String, state: EpisodeDownloadState) = EpisodeDownload(episode(id), state, addedAtMs = 0)

    private fun setDownloadsTab(downloads: List<EpisodeDownload>) = composeRule.setContent {
        ProPodcastTheme {
            FavoritesContent(
                state = FavoritesUiState(downloads = downloads, isLoading = false),
                onPodcastClick = {},
                onRemovePodcast = {},
                onSharePodcast = {},
                onEpisodeClick = { calls += "play ${it.id}" },
                onRemoveEpisode = {},
                onShareEpisode = {},
                initialTab = FavoritesTab.DOWNLOADS,
                downloads = actions,
            )
        }
    }

    @Test
    fun downloadsTab_showsStates_andButtonsAct() {
        setDownloadsTab(
            listOf(
                download("e1", EpisodeDownloadState(DownloadStatus.DOWNLOADING, progress = 0.45f)),
                download("e2", EpisodeDownloadState(DownloadStatus.DOWNLOADED, sizeBytes = 62_000_000)),
                download("e3", EpisodeDownloadState(DownloadStatus.FAILED)),
            ),
        )

        composeRule.onNodeWithText("Downloaded (3)").assertIsDisplayed()
        composeRule.onNodeWithText("Downloading 45%").assertIsDisplayed()
        composeRule.onNodeWithText("Downloaded · ", substring = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Cancel download").performClick()
        composeRule.onNodeWithContentDescription("Retry").performClick()
        composeRule.onNodeWithText("Episode e2").performClick()

        assertEquals(listOf("cancel e1", "download e3", "play e2"), calls)
    }

    @Test
    fun swipe_cancelsRunning_deletesFinished() {
        setDownloadsTab(
            listOf(
                download("e1", EpisodeDownloadState(DownloadStatus.QUEUED)),
                download("e2", EpisodeDownloadState(DownloadStatus.DOWNLOADED)),
            ),
        )

        composeRule.onNodeWithText("Episode e1").performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("Episode e2").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        assertEquals(listOf("cancel e1", "delete e2"), calls)
    }

    @Test
    fun emptyDownloads_showHint() {
        setDownloadsTab(emptyList())
        composeRule.onNodeWithText("No downloads yet.", substring = true).assertIsDisplayed()
    }

    @Test
    fun episodeRow_showsDownloadedMark() {
        composeRule.setContent {
            ProPodcastTheme {
                EpisodeListItem(
                    episode = episode("e1"),
                    isFavorite = false,
                    onClick = {},
                    onToggleFavorite = {},
                    onShare = null,
                    downloads = actions.copy(
                        states = mapOf("e1" to EpisodeDownloadState(DownloadStatus.DOWNLOADED)),
                    ),
                )
            }
        }

        composeRule.onNodeWithContentDescription("Downloaded").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("More options").performClick()
        composeRule.onNodeWithText("Delete download").performClick()
        assertEquals(listOf("delete e1"), calls)
    }

    @Test
    fun fullPlayer_downloadButtonFollowsTheState() {
        var state by mutableStateOf<EpisodeDownloadState?>(null)
        val item = NowPlaying("e1", "Episode e1", "Podcast p1", "https://example.com/e1.mp3", null, 60_000)
        composeRule.setContent {
            ProPodcastTheme {
                FullPlayerContent(
                    state = PlayerUiState(PlayerState(nowPlaying = item)),
                    onCollapse = {},
                    onTogglePlayPause = {},
                    onSeekTo = {},
                    onSeekBack = {},
                    onSeekForward = {},
                    onSpeedSelected = {},
                    downloadState = state,
                    onDownloadClick = { calls += "click" },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Download").performClick()
        state = EpisodeDownloadState(DownloadStatus.DOWNLOADING, progress = 0.5f)
        composeRule.onNodeWithContentDescription("Cancel download").assertIsDisplayed()
        state = EpisodeDownloadState(DownloadStatus.DOWNLOADED)
        composeRule.onNodeWithContentDescription("Delete download").assertIsDisplayed()

        assertEquals(listOf("click"), calls)
    }
}
