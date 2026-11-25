package com.propodcast.app.feature.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.designsystem.component.PlaceholderScreen

/** Placeholder until stage 3 (podcast details, episodes, recommendations). */
@Composable
fun PodcastDetailsScreen(
    podcastId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.podcast_details_title),
        message = stringResource(R.string.coming_soon) + "\n" + podcastId,
        onBack = onBack,
        modifier = modifier,
    )
}
