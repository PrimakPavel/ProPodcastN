package com.propodcast.app.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.ui.LocalArtworkOrigin
import com.propodcast.app.core.ui.LocalNavAnimatedVisibilityScope
import com.propodcast.app.feature.details.DetailsPreview
import com.propodcast.app.feature.details.PodcastDetailsScreen
import com.propodcast.app.feature.favorites.FavoritesScreen
import com.propodcast.app.feature.home.HomeScreen
import com.propodcast.app.feature.search.SearchScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    /** Starts playing an episode; false when it has no audio. */
    onPlayEpisode: (Episode) -> Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            Destination(this, origin = "home") {
                HomeScreen(onPodcastClick = { navController.navigate(it.detailsRoute("home")) })
            }
        }
        composable<FavoritesRoute> {
            Destination(this, origin = "favorites") {
                FavoritesScreen(
                    onPodcastClick = { navController.navigate(it.detailsRoute("favorites")) },
                    onPlayEpisode = onPlayEpisode,
                )
            }
        }
        composable<SearchRoute> {
            Destination(this, origin = "search") {
                SearchScreen(onPodcastClick = { navController.navigate(it.detailsRoute("search")) })
            }
        }
        composable<PodcastDetailsRoute> { entry ->
            val route = entry.toRoute<PodcastDetailsRoute>()
            // Recommendations get their own origin, so they fly only between this screen and their details.
            val origin = "details/${route.podcastId}"
            Destination(this, origin = origin) {
                PodcastDetailsScreen(
                    onBack = navController::navigateUp,
                    onPodcastClick = { navController.navigate(it.detailsRoute(origin)) },
                    onPlayEpisode = onPlayEpisode,
                    preview = DetailsPreview(route.title, route.artworkUrl, route.artworkKey),
                )
            }
        }
    }
}

/** Gives the screen its transition scope and artwork origin for shared element ("hero") transitions. */
@Composable
private fun Destination(
    animatedVisibilityScope: AnimatedVisibilityScope,
    origin: String,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalNavAnimatedVisibilityScope provides animatedVisibilityScope,
        LocalArtworkOrigin provides origin,
        content = content,
    )
}
