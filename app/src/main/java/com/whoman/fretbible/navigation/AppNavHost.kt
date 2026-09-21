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
private enum class Route{Home,Practice,Roadmap,Progress,More,Analyzer}
@Composable fun AppNavHost(){var route by remember{mutableStateOf<Route>(Route.Home)};Scaffold(containerColor=Background,bottomBar={NavigationBar(containerColor=Surface,tonalElevation=8.dp){listOf(Route.Home to Icons.Default.Home,Route.Practice to Icons.Default.PlayArrow,Route.Roadmap to Icons.Default.Map,Route.Progress to Icons.Default.BarChart,Route.More to Icons.Default.MoreHoriz).forEach{(item,icon)->NavigationBarItem(selected=route==item||(item==Route.More&&route==Route.Analyzer),onClick={route=item},icon={Icon(icon,contentDescription=item.name)},label={Text(if(item==Route.More)"More" else item.name)})}}}){padding->AnimatedContent(targetState=route,modifier=Modifier.padding(padding),label="screen"){current->when(current){Route.Home->HomeScreen{route=Route.Practice};Route.Practice->PracticeScreen();Route.Roadmap->RoadmapScreen();Route.Progress->ProgressScreen();Route.More->ProfileScreen{route=Route.Analyzer};Route.Analyzer->AnalyzerScreen()}}}}