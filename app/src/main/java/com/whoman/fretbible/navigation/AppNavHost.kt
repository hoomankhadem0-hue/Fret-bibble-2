package com.whoman.fretbible.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.whoman.fretbible.ui.screens.*
import com.whoman.fretbible.ui.theme.Background
import com.whoman.fretbible.ui.theme.Surface

private enum class Route(val path: String) {
    Splash("splash"), Home("home"), Practice("practice"), Roadmap("roadmap"), Progress("progress"), Profile("profile")
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val topRoutes = Route.entries.filter { it != Route.Splash }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (current in topRoutes.map { it.path }) {
                NavigationBar(containerColor = Surface) {
                    val items = listOf(
                        Route.Home to Icons.Default.Home,
                        Route.Practice to Icons.Default.PlayArrow,
                        Route.Roadmap to Icons.Default.Map,
                        Route.Progress to Icons.Default.BarChart,
                        Route.Profile to Icons.Default.Person
                    )
                    items.forEach { (route, icon) ->
                        NavigationBarItem(
                            selected = current == route.path,
                            onClick = { nav.navigate(route.path) { launchSingleTop = true; restoreState = true } },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(route.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = nav,
            startDestination = Route.Splash.path,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 12 } },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 12 } }
        ) {
            composable(Route.Splash.path) { SplashScreen { nav.navigate(Route.Home.path) { popUpTo(Route.Splash.path) { inclusive = true } } } }
            composable(Route.Home.path) { HomeScreen { nav.navigate(Route.Practice.path) } }
            composable(Route.Practice.path) { PracticeScreen() }
            composable(Route.Roadmap.path) { RoadmapScreen() }
            composable(Route.Progress.path) { ProgressScreen() }
            composable(Route.Profile.path) { ProfileScreen() }
        }
    }
}
