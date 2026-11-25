package com.propodcast.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        composable<HomeRoute> { HomeScreen() }
        composable<FavoritesRoute> { FavoritesScreen() }
        composable<SearchRoute> { SearchScreen() }
    }
}
