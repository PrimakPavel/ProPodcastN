package com.propodcast.app.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.designsystem.component.PlaceholderScreen

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.best_podcasts_title),
        message = stringResource(R.string.coming_soon),
        modifier = modifier,
    )
}
