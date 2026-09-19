package com.whoman.fretbible.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.whoman.fretbible.ui.screens.*
import com.whoman.fretbible.ui.theme.*

private enum class Route { Home, Practice, Roadmap, Progress, Profile }

@Composable
fun AppNavHost() {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Surface) {
                val items = listOf(
                    Route.Home to Icons.Default.Home,
                    Route.Practice to Icons.Default.PlayArrow,
                    Route.Roadmap to Icons.Default.Map,
                    Route.Progress to Icons.Default.BarChart,
                    Route.Profile to Icons.Default.Person
                )
                items.forEach { (item, icon) ->
                    NavigationBarItem(
                        selected = route == item,
                        onClick = { route = item },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(item.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.animation.AnimatedContent(
            targetState = route,
            modifier = Modifier.padding(padding),
            label = "screen"
        ) { current ->
            when (current) {
                Route.Home -> HomeScreen { route = Route.Practice }
                Route.Practice -> PracticeScreen()
                Route.Roadmap -> RoadmapScreen()
                Route.Progress -> ProgressScreen()
                Route.Profile -> ProfileScreen()
            }
        }
    }
}
