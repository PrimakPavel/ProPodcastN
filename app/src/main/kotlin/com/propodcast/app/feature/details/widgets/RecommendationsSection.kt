package com.propodcast.app.feature.details.widgets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.R
import com.propodcast.app.core.commonwidgets.PodcastCard
import com.propodcast.app.core.model.Podcast

/** "You may also like" title and a horizontal row of podcasts; nothing when the list is empty. */
internal fun LazyListScope.recommendationsSection(
    recommendations: List<Podcast>,
    onPodcastClick: (Podcast) -> Unit,
) {
    if (recommendations.isEmpty()) return
    item(key = "recommendations-title") { SectionTitle(stringResource(R.string.details_recommendations)) }
    item(key = "recommendations") {
        LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
            items(recommendations, key = { it.id }) { podcast ->
                PodcastCard(podcast = podcast, onClick = { onPodcastClick(podcast) })
            }
        }
    }
}
