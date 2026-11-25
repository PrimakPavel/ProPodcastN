package com.propodcast.app.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.propodcast.app.R
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.ApiQuota
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Warning about the Listen Notes quota; shows nothing unless the quota is running low. */
@Composable
fun QuotaBanner(
    quota: ApiQuota?,
    modifier: Modifier = Modifier,
) {
    if (quota == null || !quota.isRunningLow()) return
    val text = stringResource(
        if (quota.isExhausted) R.string.quota_exhausted else R.string.quota_running_low,
        quota.used,
        quota.freeQuota,
    )
    val resets = quota.nextBillingDate?.let { stringResource(R.string.quota_resets_on, it.formatDate()) }
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.errorContainer),
            leadingContent = { Icon(Icons.Filled.Warning, contentDescription = null) },
            headlineContent = {
                Column {
                    Text(text)
                    if (resets != null) Text(resets, style = MaterialTheme.typography.bodySmall)
                }
            },
        )
    }
}

private fun Instant.formatDate(): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()).format(this)

@Preview
@Composable
private fun QuotaBannerPreview() {
    ProPodcastTheme {
        QuotaBanner(ApiQuota(used = 2100, freeQuota = 2500, nextBillingDate = Instant.now(), plan = "FREE"))
    }
}
