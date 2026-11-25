package com.propodcast.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.propodcast.app.R
import kotlin.reflect.KClass

/** Destinations shown in the bottom bar / navigation rail. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    HOME(HomeRoute, HomeRoute::class, Icons.Filled.Home, R.string.best_podcasts_title),
    FAVORITES(FavoritesRoute(), FavoritesRoute::class, Icons.Filled.Favorite, R.string.favorites_title),
    SEARCH(SearchRoute, SearchRoute::class, Icons.Filled.Search, R.string.search_title),
}
