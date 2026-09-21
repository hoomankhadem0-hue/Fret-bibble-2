package com.whoman.fretbible.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.whoman.fretbible.ui.screens.*
import com.whoman.fretbible.ui.theme.*

private enum class Route { Home, Practice, Roadmap, Progress, More, Analyzer }

@Composable
fun AppNavHost() {
    var route by remember { mutableStateOf<Route>(Route.Home) }

    val items = listOf(
        Route.Home to Icons.Outlined.Home,
        Route.Practice to Icons.Outlined.PlayArrow,
        Route.Roadmap to Icons.Outlined.Map,
        Route.Progress to Icons.Outlined.BarChart,
        Route.More to Icons.Outlined.MoreHoriz
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
                    val selected = route == item || (item == Route.More && route == Route.Analyzer)
                    NavigationBarItem(
                        selected = selected,
                        onClick = { route = item },
                        icon = { Icon(icon, contentDescription = item.name) },
                        label = { Text(if (item == Route.More) "More" else item.name) },
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
                Route.Home -> HomeScreen { route = Route.Practice }
                Route.Practice -> PracticeScreen()
                Route.Roadmap -> RoadmapScreen()
                Route.Progress -> ProgressScreen()
                Route.More -> ProfileScreen { route = Route.Analyzer }
                Route.Analyzer -> AnalyzerScreen()
            }
        }
    }
}
