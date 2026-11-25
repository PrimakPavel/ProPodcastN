package com.propodcast.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.propodcast.app.feature.details.PodcastDetailsScreen
import com.propodcast.app.feature.favorites.FavoritesScreen
import com.propodcast.app.feature.home.HomeScreen
import com.propodcast.app.feature.search.SearchScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
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
        composable<FavoritesRoute> { FavoritesScreen() }
        composable<SearchRoute> { SearchScreen() }
        composable<PodcastDetailsRoute> { entry ->
            PodcastDetailsScreen(
                podcastId = entry.toRoute<PodcastDetailsRoute>().podcastId,
                onBack = navController::navigateUp,
            )
        }
    }
}
