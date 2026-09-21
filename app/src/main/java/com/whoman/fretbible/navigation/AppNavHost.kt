package com.whoman.fretbible.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.whoman.fretbible.ui.screens.*
import com.whoman.fretbible.ui.theme.*

private enum class Route { Home, Practice, Analyzer, Roadmap, Progress, Profile }

@Composable
fun AppNavHost() {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    val showBottomBar = true

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Surface,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        Route.Home to Icons.Default.Home,
                        Route.Practice to Icons.Default.PlayArrow,
                        Route.Analyzer to Icons.Default.GraphicEq,
                        Route.Roadmap to Icons.Default.Map,
                        Route.Progress to Icons.Default.BarChart,
                        Route.Profile to Icons.Default.Person
                    )
                    items.forEach { (item, icon) ->
                        NavigationBarItem(
                            selected = route == item,
                            onClick = { route = item },
                            icon = { Icon(icon, contentDescription = item.name) },
                            label = { Text(item.name) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = route,
            modifier = Modifier.padding(padding),
            label = "screen"
        ) { current ->
            when (current) {
                Route.Home -> HomeScreen { route = Route.Practice }
                Route.Practice -> PracticeScreen()
                Route.Analyzer -> AnalyzerScreen()
                Route.Roadmap -> RoadmapScreen()
                Route.Progress -> ProgressScreen()
                Route.Profile -> ProfileScreen()
            }
        }
    }
}
