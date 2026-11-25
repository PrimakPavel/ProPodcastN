package com.propodcast.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.propodcast.app.feature.player.FullPlayerSheet
import com.propodcast.app.feature.player.MiniPlayer
import com.propodcast.app.feature.player.PlayerViewModel
import com.propodcast.app.navigation.AppNavHost
import com.propodcast.app.navigation.TopLevelDestination

/** Root composable: adaptive navigation (bottom bar on phones, rail on wider screens), nav host and player. */
@Composable
fun ProPodcastApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val player by playerViewModel.uiState.collectAsStateWithLifecycle()
    var showFullPlayer by rememberSaveable { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Keeps the tab highlighted while a nested screen (e.g. podcast details) is open.
    var selectedTab by rememberSaveable { mutableStateOf(TopLevelDestination.HOME) }
    backStackEntry?.destination?.topLevelDestination()?.let { selectedTab = it }

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = destination == selectedTab,
                    onClick = { navController.navigateToTopLevel(destination) },
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { Text(stringResource(destination.labelRes)) },
                )
            }
        },
    ) {
        Column {
            AppNavHost(
                navController = navController,
                onPlayEpisode = playerViewModel::play,
                modifier = Modifier.weight(1f),
            )
            AnimatedVisibility(visible = player.isVisible, enter = expandVertically(), exit = shrinkVertically()) {
                MiniPlayer(
                    state = player,
                    onOpen = { showFullPlayer = true },
                    onTogglePlayPause = playerViewModel::togglePlayPause,
                    onClose = playerViewModel::stop,
                )
            }
        }
    }

    if (showFullPlayer && player.isVisible) {
        FullPlayerSheet(
            state = player,
            onDismiss = { showFullPlayer = false },
            onTogglePlayPause = playerViewModel::togglePlayPause,
            onSeekTo = playerViewModel::seekTo,
            onSeekBack = playerViewModel::seekBack,
            onSeekForward = playerViewModel::seekForward,
            onCycleSpeed = playerViewModel::cycleSpeed,
        )
    }
}

private fun NavDestination.topLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { destination -> hierarchy.any { it.hasRoute(destination.routeClass) } }

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
