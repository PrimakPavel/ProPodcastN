package com.propodcast.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.propodcast.app.core.model.Episode
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
            HomeScreen(onPodcastClick = { navController.navigate(PodcastDetailsRoute(it.id)) })
        }
        composable<FavoritesRoute> {
            FavoritesScreen(
                onPodcastClick = { navController.navigate(PodcastDetailsRoute(it.id)) },
                onPlayEpisode = onPlayEpisode,
            )
        }
        composable<SearchRoute> {
            SearchScreen(onPodcastClick = { navController.navigate(PodcastDetailsRoute(it.id)) })
        }
        composable<PodcastDetailsRoute> {
            PodcastDetailsScreen(
                onBack = navController::navigateUp,
                onPodcastClick = { navController.navigate(PodcastDetailsRoute(it.id)) },
                onPlayEpisode = onPlayEpisode,
            )
        }
    }
}
