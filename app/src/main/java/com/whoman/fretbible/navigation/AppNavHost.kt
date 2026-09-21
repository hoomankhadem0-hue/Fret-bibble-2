package com.whoman.fretbible.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.whoman.fretbible.ui.screens.*
import com.whoman.fretbible.ui.theme.*

private enum class Route { Home, Practice, Roadmap, Progress, Profile, Analyzer, Dictionary }

@Composable
fun AppNavHost(userName: String, onUserNameChanged: (String) -> Unit) {
    var route by remember { mutableStateOf<Route>(Route.Home) }

    val items = listOf(
        Route.Home to Icons.Outlined.Home,
        Route.Practice to Icons.Outlined.PlayArrow,
        Route.Roadmap to Icons.Outlined.Map,
        Route.Progress to Icons.Outlined.BarChart,
        Route.Profile to Icons.Outlined.Person
    )

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = Surface.copy(alpha = .98f),
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                items.forEach { (item, icon) ->
                    val selected = route == item || (item == Route.Home && (route == Route.Analyzer || route == Route.Dictionary))
                    NavigationBarItem(
                        selected = selected,
                        onClick = { route = item },
                        icon = { Icon(icon, contentDescription = item.name) },
                        label = { Text(if (item == Route.Profile) "Profile" else item.name) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Lime,
                            selectedTextColor = Lime,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = LimeSoft
                        )
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = route,
            modifier = Modifier.padding(padding),
            transitionSpec = { fadeIn(initialAlpha = .45f) togetherWith fadeOut(targetAlpha = .25f) },
            label = "screen"
        ) { current ->
            when (current) {
                Route.Home -> HomeScreen(
                    userName = userName,
                    onStartPractice = { route = Route.Practice },
                    onOpenDictionary = { route = Route.Dictionary },
                    onOpenAnalyzer = { route = Route.Analyzer }
                )
                Route.Practice -> PracticeScreen(userName = userName)
                Route.Roadmap -> RoadmapScreen()
                Route.Progress -> ProgressScreen()
                Route.Profile -> ProfileScreen(userName = userName, onUserNameChanged = onUserNameChanged)
                Route.Analyzer -> AnalyzerScreen()
                Route.Dictionary -> FretboardExplorerScreen { route = Route.Home }
            }
        }
    }
}
