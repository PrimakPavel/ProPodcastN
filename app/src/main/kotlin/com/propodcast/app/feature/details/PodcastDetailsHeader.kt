package com.propodcast.app.feature.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.R
import com.propodcast.app.core.designsystem.component.ExpandableHtmlText
import com.propodcast.app.core.designsystem.component.PodcastImage
import com.propodcast.app.core.model.Podcast

/** Artwork, title, publisher, episode count and the (HTML) description. */
@Composable
fun PodcastDetailsHeader(
    podcast: Podcast,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PodcastImage(url = podcast.imageUrl, modifier = Modifier.size(120.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(podcast.title, style = MaterialTheme.typography.titleLarge)
                if (podcast.publisher.isNotBlank()) {
                    Text(
                        text = podcast.publisher,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (podcast.totalEpisodes > 0) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.episodes_count,
                            podcast.totalEpisodes,
                            podcast.totalEpisodes,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (podcast.description.isNotBlank()) {
            Spacer(Modifier.size(12.dp))
            ExpandableHtmlText(html = podcast.description)
        }
    }
}
