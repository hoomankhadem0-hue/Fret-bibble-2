package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whoman.fretbible.practice.PracticeStatsStore
import com.whoman.fretbible.ui.components.*
import com.whoman.fretbible.ui.theme.*

@Composable
fun ProgressScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var stats by remember { mutableStateOf(PracticeStatsStore.load(context)) }

    LaunchedEffect(Unit) { stats = PracticeStatsStore.load(context) }

    val minutes = stats.practiceSeconds / 60
    val hours = minutes / 60
    val mins = minutes % 60
    val accuracyProgress = stats.accuracy.coerceIn(0, 100) / 100f

    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            kicker = "PROGRESS",
            title = "Practice, measured.",
            subtitle = "Your totals stay on this device."
        )

        SurfaceCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("OVERALL ACCURACY", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                    Text("${stats.accuracy}%", color = Lime, style = MaterialTheme.typography.displayMedium)
                    Text(
                        if (stats.attempts == 0) "Start a session to build a baseline." else "${stats.correct} correct · ${stats.attempts} attempts",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { accuracyProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = Lime,
                        trackColor = Border,
                        strokeWidth = 7.dp
                    )
                    Surface(shape = CircleShape, color = Background) {
                        Text("${stats.accuracy}", color = TextPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat("SESSIONS", stats.sessions.toString(), Modifier.weight(1f), accent = true)
            MiniStat("POINTS", stats.points.toString(), Modifier.weight(1f))
            MiniStat("BEST", stats.bestSessionPoints.toString(), Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat("CORRECT", stats.correct.toString(), Modifier.weight(1f))
            MiniStat("TIME", if (hours > 0) "${hours}h ${mins}m" else "${mins}m", Modifier.weight(1f))
        }

        SectionLabel("WHAT THE NUMBERS MEAN")
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            MetricExplanation("Accuracy", "Correct notes divided by recorded attempts.")
            MetricExplanation("Points", "Base points plus speed and streak bonuses.")
            MetricExplanation("Practice time", "Time spent inside completed sessions.")
        }

        if (stats.sessions == 0) {
            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text("NO HISTORY YET", color = Lime, style = MaterialTheme.typography.labelMedium)
                Text("Your first completed practice session will appear here.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MetricExplanation(title: String, description: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
        Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
