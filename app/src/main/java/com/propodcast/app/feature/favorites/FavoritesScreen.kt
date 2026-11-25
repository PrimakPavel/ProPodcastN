package com.propodcast.app.feature.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.propodcast.app.R
import com.propodcast.app.core.designsystem.component.PlaceholderScreen

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.favorites_title),
        message = stringResource(R.string.coming_soon),
        modifier = modifier,
    )
}
