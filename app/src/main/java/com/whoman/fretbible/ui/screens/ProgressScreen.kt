package com.whoman.fretbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.whoman.fretbible.practice.PracticeStatsStore
import com.whoman.fretbible.ui.theme.*

@Composable
fun ProgressScreen() {
    val context = LocalContext.current
    val stats = remember { PracticeStatsStore.load(context) }
    val minutes = stats.practiceSeconds / 60
    val seconds = stats.practiceSeconds % 60
    val accuracy = stats.accuracy

    Column(
        Modifier.fillMaxSize().background(Background).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("PROGRESS", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text("Your practice, saved.", color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
        Text("Every completed session adds to these stats.", color = TextSecondary)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("SESSIONS", stats.sessions.toString(), Modifier.weight(1f))
            StatCard("POINTS", stats.points.toString(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("ACCURACY", "${accuracy}%", Modifier.weight(1f))
            StatCard("BEST SESSION", stats.bestSessionPoints.toString(), Modifier.weight(1f))
        }

        Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PRACTICE TIME", color = TextMuted, style = MaterialTheme.typography.labelLarge)
                Text("${minutes}m ${seconds}s", color = Lime, style = MaterialTheme.typography.headlineMedium)
                LinearProgressIndicator(
                    progress = { (stats.practiceSeconds / 3600f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(), color = Lime, trackColor = Border
                )
                Text("${stats.correct} correct notes across ${stats.attempts} attempts.", color = TextSecondary)
            }
        }

        Text("WHAT THIS MEANS", color = TextMuted, style = MaterialTheme.typography.labelLarge)
        Text(
            if (stats.sessions == 0) "Start your first session and your real progress will appear here."
            else "Keep building consistency. Your totals are stored on this device.",
            color = TextSecondary
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = ElevatedSurface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Lime, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
